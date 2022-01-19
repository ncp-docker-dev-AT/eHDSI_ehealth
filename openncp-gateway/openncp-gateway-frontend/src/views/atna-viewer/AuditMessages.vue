<template>
  <v-container fluid>
    <div>
      <v-breadcrumbs :items="items">
        <template v-slot:divider>
          <v-icon>mdi-forward</v-icon>
        </template>
      </v-breadcrumbs>
    </div>
    <v-card>
      <v-card-text>
        <v-data-table
          :headers="headers"
          :items="messages"
          :rowsPerPageItems="[10]"
          :items-per-page="itemsPerPage"
          :options.sync="options"
          :server-items-length="totalMessages"
          :loading="loading"
        >
          <template v-slot:[`item.actions`]="{ item }">
            <v-btn
              fab
              x-small
              color="indigo"
              :to="{ name: 'audit-details', params: { id: item.id } }"
            >
              <v-icon>mdi-eye</v-icon>
            </v-btn>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script>
import axios from 'axios'

export default {
  data () {
    return {
      headers: [
        { text: '#', value: 'id', sortable: false },
        {
          text: 'Event Action Code',
          value: 'eventActionCode',
          sortable: false
        },
        { text: 'Event ID Code', value: 'eventId', sortable: false },
        { text: 'Event Type Codes', value: 'eventTypes', sortable: false },
        { text: 'Event Date Time', value: 'eventDateTime', sortable: false },
        {
          text: 'Event Outcome Indicator',
          value: 'eventOutcomeIndicator',
          sortable: false
        },
        { value: 'actions', sortable: false }
      ],
      messages: [],
      itemsPerPage: 10,
      totalMessages: 0,
      options: { page: 0, itemsPerPage: 10 },
      loading: true,
      items: [
        {
          text: 'ATNA Viewer',
          disabled: true
        },
        {
          text: 'Audit messages list',
          disabled: true
        }
      ]
    }
  },
  mounted () {
    this.getDataFromApi()
  },
  watch: {
    options: {
      handler () {
        this.getDataFromApi()
      },
      deep: true
    }
  },
  methods: {
    getDataFromApi () {
      this.loading = true
      this.apiCall().then((data) => {
        this.messages = data.data.content
        this.totalMessages = data.data.totalElements
        this.options.page = data.data.number
        this.loading = false
      })
    },
    apiCall () {
      return axios.get(process.env.VUE_APP_SERVER_URL + '/api/atna/messages', {
        params: {
          pageNumber: this.options.page,
          size: this.options.itemsPerPage
        }
      })
    }
  }
}
</script>
