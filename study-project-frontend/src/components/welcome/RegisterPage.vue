<script setup>

import {Lock, User,Message,EditPen} from "@element-plus/icons-vue";
import router from "@/router/index.js";
import {reactive,ref} from "vue";
import {ElMessage} from "element-plus";
import {post} from "@/net/index.js";

const form = reactive({
    username: '',
    password: '',
    password_repeat: '',
    email: '',
    code: '',
})
const validateUsername = (rule, value, callback) => {
  if (value === '') {
      callback(new Error('请输入用户名'))
  } else if(!/^[a-zA-Z\u4e00-\u9fa5]+$/.test(value)){
      callback(new Error("不能包含特殊字符,只可以中英文"))
  }
  else{
      callback()
  }
}

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入再次输入密码'))
  } else if(value !== form.password){
    callback(new Error("两次密码不一致"))
  }
  else{
    callback()
  }
}
const rules = {
  username: [
    { validator: validateUsername,  trigger: ['blur','change'] },
    { min: 3, max: 8, message: '长度需要3到8字符之间', trigger: ['blur','change'] },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 16, message: '长度需要6到16字符之间', trigger: ['blur','change'] },
  ],
  password_repeat: [
    { validator: validatePassword,  trigger: ['blur','change'] },
  ],
  email: [
    {
      type: 'email',
      message: '请输入合法的电子邮件',
      trigger: ['blur', 'change'],
    },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
  ]
}
const isEmailValid = ref(false)

const onValidate = (prop,isValid) => {
    if(prop === 'email')
        isEmailValid.value = isValid
}
const formRef = ref()
const register = () => {
  formRef.value.validate((isValid) => {
      if(isValid){
        post("api/auth/register",{
          ...form
        },(message) => {
            ElMessage.success(message)
            router.push("/")
        })
      } else {
          ElMessage.warning('请检查表单')
      }
  })
}

const cold = ref(0)
const timerId = ref(0)
const validateEmail = () => {
  cold.value = 60
  post('/api/auth/valid-register-email',{
    email: form.email
  },(message) => {
    ElMessage.success(message)
    timerId.value = setInterval(() => {
      if(cold.value > 0){
        cold.value--
      }
      else{
        clearInterval(timerId.value)
      }
    },1000)
  },(message) => {
    cold.value = 0
    ElMessage.warning(message)
  })
}

</script>

<template>
  <div style="text-align: center;margin: 0 20px">
    <div style="margin-top: 100px">
      <div style="font-size: 25px">注册</div>
      <div style="font-size: 14px;color:grey">欢迎注册</div>
    </div>

    <div style="margin-top: 50px">
        <el-form :model="form" :rules="rules" @validate="onValidate" ref = "formRef">
            <el-form-item prop="username">
              <el-input v-model="form.username" type="text" placeholder="用户名">
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="password">
                <el-input v-model="form.password" type="password" placeholder="密码">
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
            </el-form-item>
            <el-form-item prop="password_repeat">
              <el-input v-model="form.password_repeat" type="password" placeholder="重复密码">
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="email">
              <el-input v-model="form.email" type="email" placeholder="电子邮箱" >
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="code">
              <el-row>
                <el-col :span="14">
                  <el-input v-model="form.code" type="number" placeholder="请输入验证码">
                    <template #prefix>
                      <el-icon><EditPen /></el-icon>
                    </template>
                  </el-input>
                </el-col>
                <el-col :span="4">
                </el-col>
                <el-col :span="6" >
                  <el-button @click='validateEmail' style="width: 100%" type="success" :disabled="!isEmailValid||cold>0">
                    {{cold > 0 ? '请稍后' + cold +'秒' : '获取验证码'}}
                    </el-button>
                </el-col>
              </el-row>
            </el-form-item>
        </el-form>
    </div>


    <div style="margin-top: 30px">
        <el-button @click='register' style="width: 270px" type="warning" plain>立即注册</el-button>
    </div>
    <div style="font-size: 14px;height: 14px;line-height: 14px;margin-top: 10px" >
        <span style="color: grey">已有账号? </span>
        <el-link @click="router.push('/')" type="primary" style="translate: 0 -1px">立即登录</el-link>
    </div>
  </div>
</template>

<style scoped>

</style>