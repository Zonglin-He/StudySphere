import { get, post } from "@/net";
import { ElMessage } from "element-plus";

export const apiForumTypes = (success) =>
    get("/forum/types", success);

export const apiForumTopic = (tid, success) =>
    get(`/forum/topic?tid=${tid}`, success);

export const apiForumInteract = (tid, type, topic, message) => {
    get(`/forum/interact?tid=${tid}&type=${type}&state=${!topic[type]}`, () => {
        topic[type] = !topic[type];
        if (topic[type]) ElMessage.success(`${message} successful!`);
        else ElMessage.success(`Cancelled ${message}!`);
    });
};

export const apiForumUpdateTopic = (data, success) =>
    post("/forum/update-topic", data, success);

export const apiForumComments = (tid, page, success) =>
    get(`/forum/comments?tid=${tid}&page=${page}`, success);

export const apiForumCommentDelete = (id, success) =>
    get(`/forum/delete?id=${id}`, success);

export const apiForumCommentSubmit = (data, success) =>
    post("/forum/add-comment", data, success);

export const apiForumTopicCreate = (data, success) =>
    post("/forum/create-topic", data, success);

export const apiForumTopTopics = (success) =>
    get("/forum/top-topic", success);

export const apiForumTopicList = (page, type, success) =>
    get(`/forum/list-topic?page=${page}&type=${type}`, success);

export const apiForumWeather = (longitude, latitude, success) =>
    get(`/forum/weather?longitude=${longitude}&latitude=${latitude}`, success);

export const apiForumCollect = (success) =>
    get("/forum/collects", success);

export const apiForumCollectDelete = (tid, success) =>
    get(`/forum/interact?tid=${tid}&type=collect&state=false`, success);
