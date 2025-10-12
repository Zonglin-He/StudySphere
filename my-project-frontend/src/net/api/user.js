import { useStore } from "@/store";
import { get, post } from "@/net";
import { ElMessage } from "element-plus";
import router from "@/router";

export const apiUserInfo = (loadingRef) => {
    loadingRef.value = true;
    const store = useStore();
    get("/user/info", (data) => {
        store.user = data;
        loadingRef.value = false;
    }, (message, status) => {
        console.error("Failed to get user info:", message, status);
        loadingRef.value = false;
    });
};

export const apiAuthRegister = (data) => {
    post("/auth/register", data, () => {
        ElMessage.success("Registration successful, welcome aboard");
        router.push("/");
    });
};

export const apiAuthAskCode = (email, coldTime, type = "register") => {
    coldTime.value = 60;
    get(`/auth/ask-code?email=${email}&type=${type}`, () => {
        ElMessage.success(`Verification code has been sent to email: ${email}, please check your inbox`);
        const handle = setInterval(() => {
            coldTime.value--;
            if (coldTime.value === 0) {
                clearInterval(handle);
            }
        }, 1000);
    }, (message) => {
        ElMessage.warning(message);
        coldTime.value = 0;
    });
};

export const apiAuthRestConfirm = (data, activeRef) =>
    post("/auth/reset-confirm", data, () => activeRef.value++);

export const apiAuthResetPassword = (data) => {
    post("/auth/reset-password", data, () => {
        ElMessage.success("Password reset successful, please log in again");
        router.push("/");
    });
};

export const apiUserPrivacy = (success) =>
    get("/user/privacy", success);

export const apiUserPrivacySave = (data, loadingRef) => {
    loadingRef.value = true;
    post("/user/save-privacy", data, () => {
        ElMessage.success("Privacy settings updated successfully!");
        loadingRef.value = false;
    });
};

export const apiUserChangePassword = (data, success) =>
    post("/user/change-password", data, success);

export const apiUserDetail = (success) =>
    get("/user/details", success);

export const apiUserDetailSave = (form, success, failure) =>
    post("/user/save-details", form, success, failure);

export const apiUserModifyEmail = (form, success) =>
    post("/user/modify-email", form, success);

export const apiNotificationList = (success) =>
    get("/notification/list", success);

export const apiNotificationDeleteAll = (success) =>
    get("/notification/delete-all", success);

export const apiNotificationDelete = (id, success) =>
    get(`/notification/delete?id=${id}`, success);

export const apiUserList = (page, size, success) =>
    get(`/admin/user/list?page=${page}&size=${size}`, success);

export const apiUserDetailTotal = (id, success) =>
    get(`/admin/user/detail?id=${id}`, success);

export const apiUserSave = (data, success) =>
    post("/admin/user/save", data, success);
