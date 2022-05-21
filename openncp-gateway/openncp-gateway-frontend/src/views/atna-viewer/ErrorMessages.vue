<template>
  <v-container fluid>
    <div>
      <v-breadcrumbs :items="items">
        <template v-slot:divider>
          <v-icon>mdi-forward</v-icon>
        </template>
      </v-breadcrumbs>
    </div>
    <v-data-table
      :headers="headers"
      :items="errors"
      :disable-items-per-page="true"
      :footer-props="{
        'items-per-page-options': [10]
      }"
      :options.sync="options"
      :server-items-length="totalErrors"
      :loading="loading"
      >
    </v-data-table>
  </v-container>
</template>

<script>
import axios from 'axios'

export default {
  data () {
    return {
      loading: false,
      headers: [
        {
          text: '#',
          value: 'id',
          sortable: false
        },
        {
          text: 'Error Timestamp',
          value: 'errorTimestamp'
        },
        {
          text: 'Error Message',
          value: 'errorMessage',
          sortable: false
        },
        {
          text: 'Source IP',
          value: 'sourceIp',
          sortable: false
        },
        {
          value: 'actions',
          sortable: false
        }
      ],
      errors: [],
      totalErrors: 0,
      options: { page: 1, itemsPerPage: 15 },
      items: [
        {
          text: 'ATNA Viewer',
          disabled: true
        },
        {
          text: 'Error messages list',
          disabled: true
        }
      ]
    }
  },
  mounted () {
  },
  watch: {
    options: {
      handler () {
        this.getDataFromApi()
      }
    },
    page: 1,
    itemsPerPage: 15
  },
  methods: {
    getDataFromApi () {
      if (!this.loading) {
        this.loading = true
        axios
          .get(process.env.VUE_APP_SERVER_URL + '/api/atna/errors', {
            params: {
              pageNumber: this.options.page - 1,
              size: this.options.itemsPerPage
            }
          }).then((response) => {
            this.errors = response.data.content
            this.totalErrors = response.data.totalElements
            this.options.page = response.data.number + 1
            this.loading = false
          })
      }
    }
  }
}
</script>
