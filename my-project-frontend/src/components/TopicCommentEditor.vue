<script setup>
import {Delta, QuillEditor} from "@vueup/vue-quill";
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import {ref} from "vue";
import {ElMessage} from "element-plus";
import {apiForumCommentSubmit} from "@/net/api/forum";

const props = defineProps({
    show: Boolean,
    tid: String,
    quote: Object
})

const content = ref()

const emit = defineEmits(['close', 'comment'])

const init = () => content.value = new Delta()

function submitComment() {
    if (deltaToText(content.value).length > 2000) {
        ElMessage.warning('The number of comments has exceeded the maximum limit. ' +
            'Please reduce the content of comments')
        return
    }
    apiForumCommentSubmit({
        tid: props.tid,
        quote: props.quote ? props.quote.id : -1,
        content: JSON.stringify(content.value)
    }, () => {
        ElMessage.success('Comment on the success')
        emit('comment')
    })
}

function deltaToSimpleText(delta) {
    let str = deltaToText(JSON.parse(delta))
    if(str.length > 35) str = str.substring(0, 35) + "..."
    return str
}

function deltaToText(delta) {
    if(!delta?.ops) return ""
    let str = ""
    for (let op of delta.ops)
        str += op.insert
    return str.replace(/\s/g, "")
}

</script>

<template>
    <div>
        <el-drawer :model-value="show"
                   :title="quote ? `Make comment to: ${deltaToSimpleText(quote.content)} reply` : 'Make post and reply'"
                   @open="init" @close="emit('close')"
                   direction="btt" :size="270"
                   :close-on-click-modal="false">
            <div>
                <div>
                    <quill-editor style="height: 120px" v-model:content="content"
                                  placeholder="Please make friendly comments"/>
                </div>
                <div style="margin-top: 10px;display: flex">
                    <div style="flex: 1;font-size: 13px;color: grey">
                        Word count: {{deltaToText(content).length}}（Support up to 2000 words）
                    </div>
                    <el-button type="success" @click="submitComment" plain>comment</el-button>
                </div>
            </div>
        </el-drawer>
    </div>
</template>

<style lang="less" scoped>
:deep(.el-drawer) {
    width: 800px;
    margin: 20px auto;
    border-radius: 10px;
}
:deep(.el-drawer__header) {
    margin: 0;
}
:deep(.el-drawer__body) {
    padding: 10px;
}
</style>
