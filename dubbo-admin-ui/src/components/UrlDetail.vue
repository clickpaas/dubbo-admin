<template>

</template>

<script>
export default {
  name: "UrlDetail",
  data: () => ({
    url: {},
    data: {},
    tableData: [],
    headers: {
      'url': '',
      'address': '',
      'side': '',
      'application': '',
      'interface': '',
      'methods': '',
      'logger': '',
      'dubbo': '',
      'pid': '',
      'application.tag': '',
      'version': '',
      'generic': '',
      'revision': '',
      'retries': '',
      'check': '',
      'default.check': '',
      'lazy': '',
      'default.lazy': '',
      'payload': '',
      'default.payload': '',
      'timeout': '',
      'default.timeout': '',
      'delay': '',
      'default.delay': '',
      'default.threads': '',
      'anyhost': '',
      'timestamp': '',
    }
  }),
  mounted: function () {
    let query = this.$route.query;
    this.tableData = []
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

    let parameters = this.data.parameters
    // 拼接数据
    Object.keys(this.headers).forEach(function (key) {

    })

  },
  methods: {
    detail: function (params) {
      this.$axios.post('/optimize/server/service/url', params)
        .then(response => {
          console.log(response)
          this.data = response.data
          console.log(this.data)
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
