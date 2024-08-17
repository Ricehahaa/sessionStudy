<script setup>


import {reactive,ref} from "vue";
import {EditPen, Lock, Message} from "@element-plus/icons-vue";
import {post} from "@/net/index.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

const form = reactive({
  password: '',
  password_repeat: '',
  email: '',
  code: '',
})
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
const active = ref(0)
const cold = ref(0)
const timerId = ref(0)
const formRef = ref()
const validateEmail = () => {
  cold.value = 60
  post('/api/auth/valid-reset-email',{
    email: form.email
  },(message) => {
    ElMessage.success(message)
    setInterval(() => {
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
const isEmailValid = ref(false)
const onValidate = (prop,isValid) => {
  if(prop === 'email')
    isEmailValid.value = isValid
}

const startReset = () => {
  formRef.value.validate((isValid) => {
    if(isValid){
      post('/api/auth/start-reset',{
        ...form
      },(message) => {
        active.value++
        ElMessage.success(message)
      })
    } else {
      ElMessage.warning('请检查表单')
    }
  })
}
const doReset = () => {
  formRef.value.validate((isValid) => {
    if(isValid){
      post('/api/auth/do-reset',{
        ...form
      },(message) => {
        ElMessage.success(message)
        router.push('/')
      })
    } else {
      ElMessage.warning('请填写新密码')
    }
  })
}


</script>

<template>
  <div>
    <div style="margin: 30px 29px">
      <el-steps :active="active" finish-status="success" align-center>
        <el-step title="验证电子邮件" />
        <el-step title="重新设定密码" />
      </el-steps>
    </div>
    <transition name="el-fade-in-linear" mode="out-in">
      <div style="text-align: center;margin: 0 20px;height: 100%" v-if="active === 0">
        <div style="margin-top: 100px">
          <div style="font-size: 25px">重置密码</div>
          <div style="font-size: 14px;color:grey">请输入需要重置密码的电子邮件</div>
          <div style="margin-top: 50px">
            <el-form :model="form" :rules="rules" @validate="onValidate" ref = "formRef">
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
                  <el-col :span="6">
                    <el-button @click='validateEmail' style="width: 100%" type="success" :disabled="!isEmailValid||cold>0">
                      {{cold > 0 ? '请稍后' + cold +'秒' : '获取验证码'}}
                    </el-button>
                  </el-col>
                </el-row>
              </el-form-item>
            </el-form>
          </div>
          <div style="margin-top: 70px">
            <el-button @click='startReset()' style="width: 270px;" type="danger" plain>开始重置密码</el-button>
          </div>
        </div>
      </div>
    </transition>

    <transition name="el-fade-in-linear" mode="out-in">
      <div style="text-align: center;margin: 0 20px;height: 100%" v-if="active === 1">
        <div style="margin-top: 80px">
          <div style="font-size: 25px">重置密码</div>
          <div style="font-size: 14px;color:grey">请填写新密码</div>
        </div>
        <div style="margin-top: 50px">
          <el-form :model="form" :rules="rules" @validate="onValidate" ref = "formRef">
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="新密码">
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="password_repeat">
              <el-input v-model="form.password_repeat" type="password" placeholder="重复新密码">
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-form>
        </div>
        <div style="margin-top: 70px">
          <el-button @click='doReset()' style="width: 270px;" type="danger" plain>立即重置密码</el-button>
        </div>
      </div>
    </transition>
  </div>

</template>

<style scoped>

</style>