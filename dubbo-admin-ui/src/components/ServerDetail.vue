<template>
  <v-container grid-list-xl fluid>
    <v-layout row wrap>
      <v-flex sm12>
        <h3>{{ $t('basicInfo') }}</h3>
      </v-flex>
      <v-flex lg12>
        <v-data-table
          :items="basic"
          class="elevation-1"
          hide-actions
          hide-headers>
          <template slot="items" slot-scope="props">
            <td>{{ $t(props.item.name) }}</td>
            <td>{{ (props.item.value === 'null' ? '' : props.item.value) }}</td>
          </template>
        </v-data-table>
      </v-flex>
      <v-flex sm12>
        <h3>{{ $t('providers') }}</h3>
      </v-flex>
      <v-flex lg12>
        <template>
          <v-data-table
            class="elevation-1"
            :pagination.sync="pagination"
            :headers="headers"
            :items="providers"
          >
            <template slot="items" slot-scope="props">
              <td>{{ props.item.service }}</td>
              <td>{{ props.item.group }}</td>
              <td>{{ props.item.version }}</td>
              <td>{{ props.item.appName }}</td>
              <td>{{ props.item.tag }}</td>
              <td class="text-xs-center px-0" nowrap>
                <v-btn
                  class="tiny"
                  color='success'
                  :href='getHref(props.item.service, props.item.appName, props.item.group, props.item.version)'
                >
                  {{ $t('detail') }}
                </v-btn>
              </td>
            </template>
          </v-data-table>
        </template>
      </v-flex>
    </v-layout>
  </v-container>
</template>

<script>
export default {
  name: "ServerDetail",
  data: () => ({
    basic: [],
    headers: [],
    providers: [],
    pagination: {
      page: 1,
      rowsPerPage: 10 // -1 for All
    },
  }),
  mounted: function () {
    this.setHeaders()
    let that = this
    let query = this.$route.query
    Object.keys(query).forEach(function (key) {
      let item = {}
      item.value = query[key]
      item.name = key
      that.basic.push(item)
    })

    // 查询数据
    let address = query.ip + ':' + query.port;
    let tag = query.tag === 'null' ? '' : query.tag
    this.search(query.application, address, tag)
  },
  methods: {
    setHeaders: function () {
      this.headers = [
        {
          text: this.$t('serviceName'),
          value: 'service',
          align: 'left'
        },
        {
          text: this.$t('group'),
          value: 'group',
          align: 'left'
        },
        {
          text: this.$t('version'),
          value: 'version',
          align: 'left'
        },
        {
          text: this.$t('app'),
          value: 'application',
          align: 'left'
        },
        {
          text: this.$t('tag'),
          value: 'tag',
          align: 'left'
        },
        {
          text: this.$t('operation'),
          value: 'operation',
          sortable: false,
          width: '110px'
        }
      ]
    },
    search: function (application, address, tag) {
      this.$axios.get('./server/services', {
        params: {
          application,
          address,
          tag
        }
      }).then(response => {
        this.providers = response.data
      })

    },
    getHref: function (service, app, group, version) {
      let query = 'service=' + service + '&app=' + app
      if (group !== null) {
        query = query + '&group=' + group
      }
      if (version != null) {
        query = query + '&version=' + version
      }
      return '#/serviceDetail?' + query
    },

  },
  computed: {
    area() {
      return this.$i18n.locale
    }
  },
  watch: {
    area() {
      this.setHeaders()
    }
  },
}
</script>

<style scoped>

.tiny {
  min-width: 30px;
  height: 20px;
  font-size: 8px;
}

</style>
