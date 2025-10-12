package com.example.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.entity.vo.response.WeatherVO;
import com.example.service.WeatherService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

@Service
public class WeatherServiceImpl implements WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherServiceImpl.class);

    @Resource RestTemplate rest;

    @Value("${spring.weather.key:}")
    String key;

    @Value("${spring.weather.host:}")
    String host; // 例如 https://np5khv5bmy.re.qweatherapi.com

    @Resource StringRedisTemplate template;

    @Override
    public WeatherVO fetchWeather(double longitude, double latitude){
        return fetchFromCache(longitude, latitude);
    }

    private WeatherVO fetchFromCache(double longitude, double latitude){
        if (key == null || key.isBlank()) {
            log.warn("QWeather key missing: spring.weather.key is empty");
            return null;
        }

        final String lon = fmt2(longitude);
        final String lat = fmt2(latitude);
        final String coord = lon + "," + lat;

        // 1) 先通过 city lookup 获取 LocationID（走你的 Host）
        JSONObject location = resolveLocation(coord);

        // 如果查到了 ID，按 ID 拉天气并缓存
        if (location != null) {
            String id = location.getString("id");
            String cacheKey = "weather:id:" + id;
            String cache = template.opsForValue().get(cacheKey);
            if (cache != null) {
                try { return JSONObject.parseObject(cache).to(WeatherVO.class); } catch (Exception ignore) {}
            }
            WeatherVO vo = fetchById(id, location);
            if (vo != null) {
                template.opsForValue().set(cacheKey, JSONObject.from(vo).toJSONString(), 1, TimeUnit.HOURS);
                return vo;
            }
        }

        // 2) 兜底：直接用 “经度,纬度” 拉天气（不依赖 city lookup）
        String cacheKey = "weather:coord:" + coord;
        String cache = template.opsForValue().get(cacheKey);
        if (cache != null) {
            try { return JSONObject.parseObject(cache).to(WeatherVO.class); } catch (Exception ignore) {}
        }
        WeatherVO vo = fetchByCoord(lon, lat);
        if (vo != null) {
            template.opsForValue().set(cacheKey, JSONObject.from(vo).toJSONString(), 30, TimeUnit.MINUTES);
        }
        return vo;
    }

    private JSONObject resolveLocation(String lonlat) {
        String base = useHost() ? trim(host) : "https://geoapi.qweather.com";
        String url = base + "/geo/v2/city/lookup?location=" + urlEnc(lonlat) + "&key=" + key;
        JSONObject geo = getJson(url);
        if (geo == null) return null;
        if (!"200".equals(geo.getString("code"))) {
            log.warn("Geo lookup non-200 code={}, body={}", geo.getString("code"), geo);
            return null;
        }
        JSONArray arr = geo.getJSONArray("location");
        return (arr != null && !arr.isEmpty()) ? arr.getJSONObject(0) : null;
    }

    private WeatherVO fetchById(String id, JSONObject location){
        String base = useHost() ? trim(host) : "https://devapi.qweather.com";
        String nowUrl = base + "/v7/weather/now?location=" + id + "&key=" + key;
        JSONObject now = getJson(nowUrl);
        if (now == null || !"200".equals(now.getString("code"))) {
            log.warn("Now by id error body={}", now);
            return null;
        }

        String hourlyUrl = base + "/v7/weather/24h?location=" + id + "&key=" + key;
        JSONObject hourly = getJson(hourlyUrl);
        if (hourly == null || !"200".equals(hourly.getString("code"))) {
            log.warn("Hourly by id error body={}", hourly);
            return null;
        }
        return buildVO(location, now, hourly);
    }

    private WeatherVO fetchByCoord(String lon, String lat){
        String base = useHost() ? trim(host) : "https://devapi.qweather.com";
        String coord = lon + "," + lat;

        String nowUrl = base + "/v7/weather/now?location=" + coord + "&key=" + key;
        JSONObject now = getJson(nowUrl);
        if (now == null || !"200".equals(now.getString("code"))) {
            log.warn("Now by coord error body={}", now);
            return null;
        }

        String hourlyUrl = base + "/v7/weather/24h?location=" + coord + "&key=" + key;
        JSONObject hourly = getJson(hourlyUrl);
        if (hourly == null || !"200".equals(hourly.getString("code"))) {
            log.warn("Hourly by coord error body={}", hourly);
            return null;
        }

        JSONObject loc = new JSONObject();
        loc.put("id", coord);
        loc.put("name", "Current Location");
        loc.put("lat", lat);
        loc.put("lon", lon);

        return buildVO(loc, now, hourly);
    }

    private WeatherVO buildVO(JSONObject location, JSONObject now, JSONObject hourly){
        WeatherVO vo = new WeatherVO();
        vo.setLocation(location);
        vo.setNow(now.getJSONObject("now"));
        JSONArray arr = hourly.getJSONArray("hourly");
        vo.setHourly(arr != null ? new JSONArray(arr.stream().limit(5).toList()) : new JSONArray());
        return vo;
    }

    // ---- HTTP & gzip（只声明 gzip，避免下发 br）----
    private JSONObject getJson(String url){
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.ALL));
            headers.set("Accept-Encoding", "gzip"); // 仅 gzip，避免 br
            headers.set("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36");
            headers.set("Referer", "https://www.qweather.com/");

            HttpEntity<Void> req = new HttpEntity<>(headers);
            ResponseEntity<byte[]> resp = rest.exchange(url, HttpMethod.GET, req, byte[].class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                log.warn("HTTP {} for {}", resp.getStatusCodeValue(), url);
                return null;
            }
            byte[] body = resp.getBody();

            JSONObject gz = tryGzipToJson(body);
            if (gz != null) return gz;

            return JSONObject.parseObject(new String(body, StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.warn("Request failed: {} | {}", url, e.toString());
            return null;
        }
    }

    private JSONObject tryGzipToJson(byte[] data){
        if (data == null || data.length == 0) return null;
        try (ByteArrayInputStream bin = new ByteArrayInputStream(data);
             GZIPInputStream gzip = new GZIPInputStream(bin);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = gzip.read(buf)) != -1) out.write(buf, 0, n);
            return JSONObject.parseObject(new String(out.toByteArray(), StandardCharsets.UTF_8));
        } catch (Exception ignore) { return null; }
    }

    private static String fmt2(double v){
        return String.format(java.util.Locale.US, "%.2f", v);
    }
    private static String urlEnc(String s){
        try { return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8); }
        catch (Exception e) { return s; }
    }
    private boolean useHost(){ return host != null && !host.isBlank(); }
    private String trim(String s){ return s.endsWith("/") ? s.substring(0, s.length()-1) : s; }
}
