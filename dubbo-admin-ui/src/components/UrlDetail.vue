<template>
  <v-container grid-list-xl fluid>
    <v-layout row wrap>
      <v-flex sm12>
        <h3>{{ side === 'provider' ? '提供者' : '消费者' }}{{ $t('urlInfo') }}</h3>
      </v-flex>
      <v-flex lg12>
        <v-data-table
          :items="basic"
          class="elevation-1"
          hide-actions
          show-expand
          calculate-widths
          hide-headers>
          <template slot="items" slot-scope="props">
            <td>{{ $t(props.item.name) }}</td>
            <td>{{ props.item.value }}</td>
          </template>
        </v-data-table>
      </v-flex>
    </v-layout>
  </v-container>

</template>

<script>
import moment from 'moment'

export default {
  name: "UrlDetail",
  data: () => ({
    side: {},
    basic: [],
    headers: {
      'url': '',
      'address': '',
      'side': '',
      'application': '',
      'application.tag': '',
      'interface': '',
      'methods': '',
      'logger': '',
      'dubbo': '',
      'pid': '',
      'timestamp': '',
      'version': '',
      'generic': '',
      'revision': '',
      'retries': '',
      'timeout': '',
      'default.timeout': '',
      'anyhost': '',
      'check': '',
      'default.check': '',
      'lazy': '',
      'default.lazy': '',
      'payload': '',
      'default.payload': '',
      'delay': '',
      'default.delay': '',
      'default.threads': '',
    }
  }),
  mounted: function () {
    let query = this.$route.query;
    let params = {
      application: query.application,
      ip: query.ip,
      port: query.port,
      tag: this.parseString(query.tag),
      service: query.service,
      group: this.parseString(query.group),
      version: this.parseString(query.version),
      side: query.side
    }
    this.detail(params)
  },
  methods: {
    detail: function (params) {
      this.$axios.post('/optimize/server/service/url', params)
        .then(response => {

          let that = this
          this.basic = []
          let headers = this.headers
          let data = response.data
          let parameters = data.parameters

          // 基础信息
          headers.url = data.url
          headers.address = data.address
          headers.application = data.application
          headers.side = that.side = data.side
          // 存在的数据
          Object.keys(parameters).forEach(function (key) {
            if (key in headers) {
              headers[key] = parameters[key]
              if (key === 'timestamp') {
                headers[key] = moment(parseInt(parameters[key])).format('YYYY-MM-DD HH:mm:ss') + ' (' + parameters[key] + ')'
              }
            }
          })

          Object.keys(headers).forEach(function (key) {
            let item = {}
            item.value = headers[key]
            item.name = key
            that.basic.push(item)
          })
        })
    },

    parseString: function (str) {
      if (str === undefined) return ''
      if (str === 'undefined') return ''
      if (str === 'null') return ''
      if (str === null) return ''
      return str;
    }

  }
}
</script>

<style scoped>

</style>
