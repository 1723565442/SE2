<script setup lang="ts">

import { request } from "~/utils/request";
import {ElMessage, ElNotification} from "element-plus";
import {h, onMounted, reactive, ref, watch} from "vue";
import { useStationsStore } from "~/stores/stations";
import { parseDate } from "~/utils/date";
import { useRouter } from "vue-router";
import { OrderDetailData } from "~/utils/interfaces";

const router = useRouter()
const stations = useStationsStore()

const props = defineProps({
  id: Number,
})

let dialog = ref(false)
let orderDetail = reactive<{ data: OrderDetailData }>({
  data: {
    id: 0,
    train_id: 0,
    seat: '',
    status: '',
    created_at: '',
    start_station_id: 0,
    end_station_id: 0,
    departure_time: '',
    arrival_time: '',
    //TODO
    price: 0,
    balance: 0,
    payment_strategy: '',
    bonus_points: 0,
    consumption_points: 0,
    left_points: 0,
    use_points: false,
  },
})

let train = reactive<{ data: { name?: string } }>({
  data: {}
});

const getOrderDetail = () => {
  request({
    url: `/order/${props.id}`,
    method: 'GET',
  }).then(res => {
    orderDetail.data = res.data.data
    console.log(orderDetail.data)
  }).catch(err => {
    console.log(err)
    if (err.response?.data.code == 100003) {
      router.push('/login')
    }
    ElNotification({
      offset: 70,
      title: 'getOrder错误',
      message: h('i', { style: 'color: teal' }, err.response?.data.msg),
    })
  })
}

const getTrain = () => {
  console.log("getTrain")
  if (orderDetail.data) {
    request({
      url: `/train/${orderDetail.data.train_id}`,
      method: 'GET'
    }).then((res) => {
      train.data = res.data.data
      console.log(train)
    }).catch((error) => {
      ElNotification({
        offset: 70,
        title: 'getTrain错误(orderDetail)',
        message: h('error', { style: 'color: teal' }, error.response?.data.msg),
      })
      console.log(error)
    })
  }
}
const pay = (id: number) => {
    request({
        url: `/order/${id}`,
        method: 'PATCH',
        data: {
            status: '已支付',
            use_points: orderDetail.data.use_points,
            payment_strategy: orderDetail.data.payment_strategy
        }
    }).then((res) => {
        ElNotification({
            offset: 70,
            title: '支付成功',
            message: h('success', { style: 'color: teal' }, res.data.msg),
        })
        getOrderDetail()
        console.log(res)
    }).catch((error) => {
        if (error.response?.data.code == 100003) {
            router.push('/login')
        }
        ElNotification({
            offset: 70,
            title: '支付失败',
            message: h('error', { style: 'color: teal' }, error.response?.data.msg),
        })
        getOrderDetail()
        console.log(error)
    })
}
//TODO: 是否使用积分
const changeUsePoints = () => {
    orderDetail.data.use_points = !orderDetail.data.use_points
}
const cancel = (id: number) => {
  request({
    url: `/order/${id}`,
    method: 'PATCH',
    data: {
      status: '已取消'
    }
  }).then((res) => {
    ElNotification({
      offset: 70,
      title: '取消成功',
      message: h('success', { style: 'color: teal' }, res.data.msg),
    })
    getOrderDetail()
    console.log(res)
  }).catch((error) => {
    if (error.response?.data.code == 100003) {
      router.push('/login')
    }
    ElNotification({
      offset: 70,
      title: '取消失败',
      message: h('error', { style: 'color: teal' }, error.response?.data.msg),
    })
    console.log(error)
  })
}

watch(orderDetail, () => {
  getTrain()
})

onMounted(() => {
  stations.fetch()
  getOrderDetail()
})

getOrderDetail()

</script>

<template>
  <div style="display: flex; flex-direction: column">

    <div style="margin-bottom: 2vh;">
      <el-button style="float:right" @click="getOrderDetail">
        刷新
      </el-button>
    </div>

    <div style="display: flex; justify-content: space-between;">
      <div>
        <el-text size="large" tag="b" type="primary">
          订单号:&nbsp;&nbsp;
        </el-text>
        <el-text size="large" tag="b">
          {{ props.id }}
        </el-text>
      </div>
      <div>
        <el-text size="large" tag="b" type="primary">
          创建日期:&nbsp;&nbsp;
        </el-text>
        <el-text size="large" tag="b" v-if="orderDetail.data">
          {{ parseDate(orderDetail.data.created_at) }}
        </el-text>
      </div>
    </div>

    <div>
      <el-text size="large" tag="b" type="primary">
        订单状态:&nbsp;&nbsp;
      </el-text>
      <el-text size="large" tag="b" v-if="orderDetail.data">
        {{ orderDetail.data.status }}
      </el-text>
    </div>
    <div>
      <el-text size="large" tag="b" type="primary">
          价格:
      </el-text>
      <el-text size="large" tag="b" v-if="orderDetail.data">
          {{ orderDetail.data.price }}
      </el-text>
    </div>
    <div>
      <el-text size="large" tag="b" type="primary">
          奖励积分:
      </el-text>
      <el-text size="large" tag="b" v-if="orderDetail.data">
          {{ orderDetail.data.bonus_points }}
      </el-text>
    </div>
    <div style="margin-bottom: 2vh">
      <el-text size="large" tag="b" type="primary">
        车次信息:
      </el-text>
    </div>
    <el-descriptions :column="4" border>
      <el-descriptions-item :span="2" width="25%" align="center">
        <template #label>
          <el-text type="primary" tag="b" size="large">
            车次
          </el-text>
        </template>
        <el-text type="primary" tag="b" size="large">
          {{ train?.data?.name }}
        </el-text>
      </el-descriptions-item>
      <el-descriptions-item label="席位信息" :span="2" width="25%" align="center" v-if="orderDetail.data">
        {{ orderDetail.data.seat }}
      </el-descriptions-item>
      <el-descriptions-item label="出发站" :span="2" width="25%" align="center" v-if="orderDetail.data">
        {{ stations.idToName[orderDetail.data.start_station_id] ?? '未知站点' }}
      </el-descriptions-item>
      <el-descriptions-item label="到达站" :span="2" width="25%" align="center" v-if="orderDetail.data">
        {{ stations.idToName[orderDetail.data.end_station_id] ?? '未知站点' }}
      </el-descriptions-item>
      <el-descriptions-item label="出发时间" :span="2" width="25%" align="center" v-if="orderDetail.data">
        {{ parseDate(orderDetail.data.departure_time) }}
      </el-descriptions-item>
      <el-descriptions-item label="到达时间" :span="2" width="25%" align="center" v-if="orderDetail.data">
        {{ parseDate(orderDetail.data.arrival_time) }}
      </el-descriptions-item>
    </el-descriptions>


    <div style="margin-top: 2vh" v-if="orderDetail.data && orderDetail.data.status === '等待支付'">
      <div style="float:left;">
          <el-button type="primary" :style="{ backgroundColor: orderDetail.data.use_points ? 'green' : 'gray' }" @click="changeUsePoints">
              使用积分
          </el-button>
      </div>
      <div style="float:right;">
        <el-select v-model="orderDetail.data.payment_strategy" placeholder="支付方式" style="margin-right: 20px;">
            <el-option label="微信" value='微信'></el-option>
            <el-option label="支付宝" value='支付宝'></el-option>
        </el-select>
        <el-button type="danger" @click="cancel(id ?? -1)">
          取消订单
        </el-button>
        <el-button type="primary" @click="pay(id ?? -1); ; dialog = true">
          支付订单
        </el-button>
      </div>
    </div>
    <div v-else-if="orderDetail.data && orderDetail.data.status === '已支付'" style="margin-top: 2vh">
      <div style="float:right;">
        <el-button @click="cancel(id ?? -1)">
          取消订单
        </el-button>
      </div>
    </div>

  </div>
    <el-dialog destroy-on-close v-model="dialog" title="支付详情" width="50%" @close="getOrderDetail">
        <div style="margin-left: 20px">
            <div>
                <el-text size="large" tag="b" type="primary">
                    价格:
                </el-text>
                <el-text size="large" tag="b">
                    {{ orderDetail.data.price }}
                </el-text>
            </div>
            <div>
                <el-text size="large" tag="b" type="primary">
                    您的{{ orderDetail.data.payment_strategy }}余额为:
                </el-text>
                <el-text size="large" tag="b">
                    {{ orderDetail.data.balance }}
                </el-text>
            </div>
            <div>
                <el-text size="large" tag="b" type="primary">
                    奖励积分:
                </el-text>
                <el-text size="large" tag="b">
                    {{ orderDetail.data.bonus_points }}
                </el-text>
            </div>
            <div>
                <el-text size="large" tag="b" type="primary">
                    消耗积分:
                </el-text>
                <el-text size="large" tag="b">
                    {{ orderDetail.data.consumption_points }}
                </el-text>
            </div>
            <div>
                <el-text size="large" tag="b" type="primary">
                    您的剩余里程积分为:
                </el-text>
                <el-text size="large" tag="b">
                    {{ orderDetail.data.left_points }}
                </el-text>
            </div>
        </div>

        <template #footer>
            <div class="dialog-footer" style="text-align: center">
                <el-button type="primary" size="large" @click="dialog = false" @close="getOrderDetail">确认</el-button>
            </div>
        </template>
    </el-dialog>

</template>

<style scoped></style>