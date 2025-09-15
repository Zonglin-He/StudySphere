package com.example.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.entity.vo.response.WeatherVO;
import com.example.service.WeatherService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

@Service
public class WeatherServiceImpl implements WeatherService {

    @Resource
    RestTemplate rest;

    @Value("${spring.weather.key}")
    String key;

    @Resource
    StringRedisTemplate template;

    @Override
    public WeatherVO fetchWeather(double longitude, double latitude){
        return fetchFromCache(longitude, latitude);
    }

    private WeatherVO fetchFromCache(double longitude, double latitude){
        // 1) QWeather 要经度在前、纬度在后
        String geoUrl = "https://geoapi.qweather.com/v2/city/lookup?location="
                + longitude + "," + latitude + "&key=" + key;

        JSONObject geo = getJson(geoUrl);
        if (geo == null) return null;

        JSONArray locations = geo.getJSONArray("location");
        if (locations == null || locations.isEmpty()) return null;

        JSONObject location = locations.getJSONObject(0);
        // 2) 按 String 处理更稳
        String locationId = location.getString("id");

        String cacheKey = "weather:" + locationId; // 避免与 API key 混淆
        String cache = template.opsForValue().get(cacheKey);
        if (cache != null) return JSONObject.parseObject(cache).to(WeatherVO.class);

        WeatherVO vo = fetchFromAPI(locationId, location);
        if (vo == null) return null;

        template.opsForValue().set(cacheKey, JSONObject.from(vo).toJSONString(), 1, TimeUnit.HOURS);
        return vo;
    }

    private WeatherVO fetchFromAPI(String id, JSONObject location){
        WeatherVO vo = new WeatherVO();
        vo.setLocation(location);

        // 3) 实况天气的正确地址
        String nowUrl = "https://devapi.qweather.com/v7/weather/now?location=" + id + "&key=" + key;
        JSONObject now = getJson(nowUrl);
        if (now == null) return null;
        vo.setNow(now.getJSONObject("now"));

        String hourlyUrl = "https://devapi.qweather.com/v7/weather/24h?location=" + id + "&key=" + key;
        JSONObject hourly = getJson(hourlyUrl);
        if (hourly == null) return null;
        vo.setHourly(new JSONArray(hourly.getJSONArray("hourly").stream().limit(5).toList()));
        return vo;
    }

    // 4) 兼容 gzip 和非 gzip 两种返回
    private JSONObject getJson(String url){
        try {
            byte[] bytes = rest.getForObject(url, byte[].class);
            JSONObject gz = decompressStringToJson(bytes);
            if (gz != null) return gz; // gzip 成功
            // 非 gzip 返回，按字符串解析
            return JSONObject.parseObject(new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    private JSONObject decompressStringToJson(byte[] data){
        if (data == null || data.length == 0) return null;
        try (ByteArrayInputStream bin = new ByteArrayInputStream(data);
             GZIPInputStream gzip = new GZIPInputStream(bin);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int read;
            while((read = gzip.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }
            return JSONObject.parseObject(out.toString(java.nio.charset.StandardCharsets.UTF_8));
        } catch (IOException e) {
            return null; // 不是 gzip 就返回 null，走非 gzip 分支
        }
    }
}

