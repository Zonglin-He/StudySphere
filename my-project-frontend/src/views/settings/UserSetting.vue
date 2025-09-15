<script setup>

import Card from "@/components/Card.vue";
import {Setting, Switch, Lock} from "@element-plus/icons-vue";
import {onMounted, reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import {apiUserChangePassword, apiUserPrivacy, apiUserPrivacySave} from "@/net/api/user";

const form = reactive({
  password: '',
  new_password: '',
  new_password_repeat: ''
})
const validatePassword = (_, value, callback) => {
  if (value === '') {
    callback(new Error('Please re-enter the password'))
  } else if (value !== form.new_password) {
    callback(new Error("The two passwords do not match"))
  } else {
    callback()
  }
}
const rules = {
  password: [
    { required: true, message: 'Please enter your current password', trigger: 'blur' }
  ],
  new_password: [
    { required: true, message: 'Please enter a new password', trigger: 'blur' },
    { min: 6, max: 16, message: 'Password length must be between 6 and 16 characters', trigger: ['blur'] }
  ],
  new_password_repeat: [
    { required: true, message: 'Please re-enter the new password', trigger: 'blur' },
    { validator: validatePassword, trigger: ['blur', 'change'] },
  ]
}
const formRef = ref()
const valid = ref(false)
const onValidate = (prop, isValid) => valid.value = isValid

function resetPassword(){
  formRef.value.validate(valid => {
    if(valid) {
      apiUserChangePassword(form, () => {
        ElMessage.success('Password changed successfully!')
        formRef.value.resetFields();
      })
    }
  })
}

const saving = ref(true)
const privacy = reactive({
  phone: false,
  wx: false,
  qq: false,
  email: false,
  gender: false
})

function savePrivacy(type, status){
  apiUserPrivacySave({ type, status }, saving)
}

onMounted(() => {
  apiUserPrivacy(data => {
    Object.assign(privacy, data)
    saving.value = false
  })
})
</script>

<template>
  <div style="margin: auto;max-width: 600px">
    <div style="margin-top: 20px">
      <card :icon="Setting" title="Privacy Settings" desc="Set which information can be seen by others. Please pay attention to your privacy." v-loading="saving">
        <div class="checkbox-list">
          <el-checkbox @change="savePrivacy('phone', privacy.phone)"
                       v-model="privacy.phone">Publicly display my phone number</el-checkbox>
          <el-checkbox @change="savePrivacy('email', privacy.email)"
                       v-model="privacy.email">Publicly display my email address</el-checkbox>
          <el-checkbox @change="savePrivacy('wx', privacy.wx)"
                       v-model="privacy.wx">Publicly display my WeChat ID</el-checkbox>
          <el-checkbox @change="savePrivacy('qq', privacy.qq)"
                       v-model="privacy.qq">Publicly display my QQ number</el-checkbox>
          <el-checkbox @change="savePrivacy('gender', privacy.gender)"
                       v-model="privacy.gender">Publicly display my gender</el-checkbox>
        </div>
      </card>
      <card style="margin: 20px 0" :icon="Setting"
            title="Change Password" desc="Change your password here. Please make sure to remember your new password.">
        <el-form :rules="rules" :model="form" ref="formRef" @validate="onValidate" label-width="100" style="margin: 20px">
          <el-form-item label="Current Password" prop="password">
            <el-input type="password" :prefix-icon="Lock" v-model="form.password"
                      placeholder="Current password" maxlength="16"/>
          </el-form-item>
          <el-form-item label="New Password" prop="new_password">
            <el-input type="password" :prefix-icon="Lock" v-model="form.new_password"
                      placeholder="New password" maxlength="16"/>
          </el-form-item>
          <el-form-item label="Confirm New Password" prop="new_password_repeat">
            <el-input type="password" :prefix-icon="Lock" v-model="form.new_password_repeat"
                      placeholder="Re-enter new password" maxlength="16"/>
          </el-form-item>
          <div style="text-align: center">
            <el-button @click="resetPassword" :icon="Switch" type="success">Reset Password Now</el-button>
          </div>
        </el-form>
      </card>
    </div>
  </div>
</template>

<style scoped>
.checkbox-list {
  margin: 10px 0 0 10px;
  display: flex;
  flex-direction: column;
}
</style>
