<template>

  <v-container id="search" grid-list-xl fluid>
    <v-layout row wrap>
      <v-flex lg12>
        <v-card flat color="transparent">
          <v-card-text>
            <v-form>
              <v-layout row wrap>
                <v-combobox
                  id="serverSearch"
                  :loading="searchLoading"
                  :items="typeAhead"
                  :search-input.sync="input"
                  v-model="keyword"
                  flat
                  append-icon=""
                  hide-no-data
                  :suffix="queryBy"
                  :label="$t('searchDubboService')"
                  @keyup.enter="submit"
                ></v-combobox>
                <v-menu class="hidden-xs-only">
                  <v-btn slot="activator" large icon>
                    <v-icon>unfold_more</v-icon>
                  </v-btn>

                  <v-list>
                    <v-list-tile
                      v-for="(item, i) in items"
                      :key="i"
                      @click="selected = i">
                      <v-list-tile-title>{{ $t(item.title) }}</v-list-tile-title>
                    </v-list-tile>
                  </v-list>
                </v-menu>
                <v-btn @click="submit" color="primary" large>{{ $t('search') }}</v-btn>
              </v-layout>
            </v-form>
          </v-card-text>
        </v-card>
      </v-flex>
    </v-layout>

    <v-flex lg12>
      <v-card>
        <v-toolbar flat color="transparent" class="elevation-0">
          <v-toolbar-title><span class="headline">{{ $t('searchResult') }}</span></v-toolbar-title>
          <v-spacer></v-spacer>
        </v-toolbar>

        <v-card-text class="pa-0">
          <template>
            <v-data-table
              class="elevation-0 table-striped"
              :pagination.sync="pagination"
              :total-items="totalItems"
              :headers="headers"
              :items="services"
              :loading="loadingServices"
            >
              <template slot="items" slot-scope="props">
                <td>{{ props.item.application }}</td>
                <td>{{ props.item.ip }}</td>
                <td>{{ props.item.port }}</td>
                <td>{{ props.item.tag }}</td>
                <td>{{ props.item.exportServices }}</td>
                <td>{{ props.item.status ? '开启' : '禁用' }}</td>
                <td class="text-xs-center px-0" nowrap>
                  <v-btn
                    class="tiny"
                    color='success'
                    :href='getHref(props.item)'
                  >
                    {{ $t('detail') }}
                  </v-btn>
                  <v-btn
                    small
                    class="tiny"
                    outline
                    :disabled="statusDisable"
                    @click='operationStatus(props.item)'
                  >
                    {{ $t(props.item.status ? 'disabled' : 'enabled') }}
                  </v-btn>
                </td>
              </template>
            </v-data-table>
          </template>
          <v-divider></v-divider>
        </v-card-text>
      </v-card>
    </v-flex>
  </v-container>
</template>

<script>
export default {
  data: () => ({
    // 搜索条件部分
    items: [
      {id: 0, title: 'app', value: 'application'},
      {id: 1, title: 'ip', value: 'ip'},
      {id: 2, title: 'tag', value: 'tag'}
    ],
    selected: 0,
    keyword: '',
    searchLoading: false,
    input: null,
    typeAhead: [],

    // 内容表格部分
    headers: [],
    timerID: null,
    statusDisable: false,
    resultPage: {},
    pagination: {
      page: 1,
      rowsPerPage: 10 // -1 for All
    },
    totalItems: 0,
    loadingServices: false
  }),
  mounted: function () {
    this.setHeaders()
    this.submit()
    this.$store.dispatch('loadAppItems')
  },
  methods: {
    operationStatus: function (item) {

      this.statusDisable = true

      setTimeout(() => {
        this.statusDisable = false   //点击一次时隔两秒后才能再次点击
      }, 2000)

      this.$axios.put('/optimize/server/status', item)
        .then(response => {
          if (response.status === 200) {
            this.submit()
            this.$notify.success((item.status ? 'Disabled' : 'Enabled') + ' success')
          }
        })
    },
    setHeaders: function () {
      this.headers = [
        {
          text: this.$t('application'),
          value: 'application',
          align: 'left'
        },
        {
          text: this.$t('ip'),
          value: 'ip',
          align: 'left'
        },
        {
          text: this.$t('port'),
          value: 'port',
          align: 'left'
        },
        {
          text: this.$t('tag'),
          value: 'tag',
          align: 'left'
        },
        {
          text: this.$t('exportServices'),
          value: 'exportServices',
          align: 'left'
        },
        {
          text: this.$t('status'),
          value: 'status',
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
    querySelections(v) {
      if (this.timerID) {
        clearTimeout(this.timerID)
      }
      // Simulated ajax query
      this.timerID = setTimeout(() => {
        if (v && v.length >= 4) {
          this.searchLoading = true
          if (this.selected === 0) {
            this.typeAhead = this.$store.getters.getAppItems(v)
          }
          this.searchLoading = false
          this.timerID = null
        } else {
          this.typeAhead = []
        }
      }, 500)
    },
    getHref: function (params) {
      let query = 'application=' + params.application + '&ip=' + params.ip
      + '&port=' + params.port + '&tag=' + params.tag;
      return '#/serverDetail?' + query
    },
    submit() {
      this.keyword = document.querySelector('#serverSearch').value.trim()
      const type = this.items[this.selected].value
      this.search(this.keyword, type, true)
    },
    search: function (keyword, type, rewrite) {
      const page = this.pagination.page - 1
      const size = this.pagination.rowsPerPage === -1 ? this.totalItems : this.pagination.rowsPerPage
      this.loadingServices = true
      this.$axios.get('/optimize/servers', {
        params: {
          type,
          keyword,
          page,
          size
        }
      }).then(response => {
        this.resultPage = response.data
        this.totalItems = this.resultPage.totalElements
      }).finally(() => {
        this.loadingServices = false
      })
    },
  },
  computed: {
    queryBy() {
      return this.$t('by') + this.$t(this.items[this.selected].title)
    },
    area() {
      return this.$i18n.locale
    },
    services() {
      if (!this.resultPage || !this.resultPage.content) {
        return []
      }
      return this.resultPage.content
    }
  },
  watch: {
    input(val) {
      this.querySelections(val)
    },
    area() {
      this.setHeaders()
    },
    pagination: {
      handler(newVal, oldVal) {
        if (newVal.page === oldVal.page && newVal.rowsPerPage === oldVal.rowsPerPage) {
          return
        }
        this.submit()
      },
      deep: true
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
