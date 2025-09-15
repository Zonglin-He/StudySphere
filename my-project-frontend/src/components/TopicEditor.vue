<script setup>
import {Check, Document} from "@element-plus/icons-vue";
import {computed, reactive, ref} from "vue";
import {Delta, Quill, QuillEditor} from "@vueup/vue-quill";
import ImageResize from "quill-image-resize-vue";
import { ImageExtend, QuillWatch } from "quill-image-super-solution-module";
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import axios from "axios";
import {accessHeader} from "@/net";
import {ElMessage} from "element-plus";
import ColorDot from "@/components/ColorDot.vue";
import {useStore} from "@/store";
import {apiForumTopicCreate} from "@/net/api/forum";

const store = useStore()

const props = defineProps({
  show: Boolean,
  defaultTitle: {
    default: '',
    type: String
  },
  defaultText: {
    default: '',
    type: String
  },
  defaultType: {
    default: null,
    type: Number
  },
  submitButton: {
    default: 'Post Topic Now',
    type: String
  },
  submit: {
    default: (editor, success) => {
      apiForumTopicCreate({
        type: editor.type.id,
        title: editor.title,
        content: editor.text
      }, () => {
        ElMessage.success("Post published successfully!")
        success()
      })
    },
    type: Function
  }
})

const emit = defineEmits(['close', 'success'])

const refEditor = ref()
const editor = reactive({
  type: null,
  title: '',
  text: '',
  loading: false
})

function initEditor() {
  if(props.defaultText)
    editor.text = new Delta(JSON.parse(props.defaultText))
  else
    refEditor.value.setContents('', 'user')
  editor.title = props.defaultTitle
  editor.type = findTypeById(props.defaultType)
}

function deltaToText(delta) {
  if(!delta.ops) return ""
  let str = ""
  for (let op of delta.ops)
    str += op.insert
  return str.replace(/\s/g, "")
}

const contentLength = computed(() => deltaToText(editor.text).length)

function findTypeById(id){
  for (let type of store.forum.types) {
    if(type.id === id)
      return type
  }
}

function submitTopic() {
  const text = deltaToText(editor.text)
  if(text.length > 20000) {
    ElMessage.warning('Word count exceeds the limit, unable to publish!')
    return
  }
  if(!editor.title) {
    ElMessage.warning('Please enter a title!')
    return
  }
  if(!editor.type) {
    ElMessage.warning('Please select a suitable topic type!')
    return
  }
  props.submit(editor, () => emit('success'))
}

Quill.register('modules/imageResize', ImageResize)
Quill.register('modules/ImageExtend', ImageExtend)
const editorOption = {
  modules: {
    toolbar: {
      container: [
        "bold", "italic", "underline", "strike","clean",
        {color: []}, {'background': []},
        {size: ["small", false, "large", "huge"]},
        { header: [1, 2, 3, 4, 5, 6, false] },
        {list: "ordered"}, {list: "bullet"}, {align: []},
        "blockquote", "code-block", "link", "image",
        { indent: '-1' }, { indent: '+1' }
      ],
      handlers: {
        'image': function () {
          QuillWatch.emit(this.quill.id)
        }
      }
    },
    imageResize: {
      modules: [ 'Resize', 'DisplaySize' ]
    },
    ImageExtend: {
      action:  axios.defaults.baseURL + '/api/image/cache',
      name: 'file',
      size: 5,
      loading: true,
      accept: 'image/png, image/jpeg',
      response: (resp) => {
        if(resp.data) {
          return axios.defaults.baseURL + '/images' + resp.data
        } else {
          return null
        }
      },
      methods: 'POST',
      headers: xhr => {
        xhr.setRequestHeader('Authorization', accessHeader().Authorization);
      },
      start: () => editor.uploading = true,
      success: () => {
        ElMessage.success('Image uploaded successfully!')
        editor.uploading = false
      },
      error: () => {
        ElMessage.warning('Image upload failed, please contact admin!')
        editor.uploading = false
      }
    }
  }
}
</script>

<template>
  <el-drawer :model-value="show"
             direction="btt"
             @open="initEditor"
             :close-on-click-modal="false"
             :size="650"
             @close="emit('close')">
    <template #header>
      <div>
        <div style="font-weight: bold">Post a New Topic</div>
        <div style="font-size: 13px">Before posting, please comply with relevant laws and regulations. Avoid offensive or inappropriate language.</div>
      </div>
    </template>
    <div style="display: flex;gap: 10px">
      <div style="width: 150px">
        <el-select placeholder="Select topic type..." value-key="id" v-model="editor.type" :disabled="!store.forum.types.length">
          <el-option v-for="item in store.forum.types.filter(type => type.id > 0)" :value="item" :label="item.name">
            <div>
              <color-dot :color="item.color"/>
              <span style="margin-left: 10px">{{item.name}}</span>
            </div>
          </el-option>
        </el-select>
      </div>
      <div style="flex: 1">
        <el-input v-model="editor.title" placeholder="Enter topic title..." :prefix-icon="Document"
                  style="height: 100%" maxlength="30"/>
      </div>
    </div>
    <div style="margin-top: 5px;font-size: 13px;color: grey">
      <color-dot :color="editor.type ? editor.type.color : '#dedede'"/>
      <span style="margin-left: 5px">{{editor.type ? editor.type.desc : 'Please select a topic type above'}}</span>
    </div>
    <div style="margin-top: 10px;height: 440px;overflow: hidden;border-radius: 5px"
         v-loading="editor.uploading"
         element-loading-text="Uploading image, please wait...">
      <quill-editor v-model:content="editor.text" style="height: calc(100% - 45px)"
                    content-type="delta" ref="refEditor"
                    placeholder="What would you like to share today?" :options="editorOption"/>
    </div>
    <div style="display: flex;justify-content: space-between;margin-top: 5px">
      <div style="color: grey;font-size: 13px">
        Current word count {{contentLength}} (maximum 20000 characters)
      </div>
      <div>
        <el-button type="success" :icon="Check" @click="submitTopic" plain>{{submitButton}}</el-button>
      </div>
    </div>
  </el-drawer>
</template>

<style scoped>
:deep(.el-drawer) {
  width: 800px;
  margin: auto;
  border-radius: 10px 10px 0 0;
}
:deep(.el-drawer__header) {
  margin: 0;
}
</style>
