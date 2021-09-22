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
      <v-card-title>
        <v-spacer></v-spacer>
        <v-text-field
          v-model="search"
          append-icon="mdi-magnify"
          label="Search"
          single-line
          hide-details
        ></v-text-field>
      </v-card-title>
      <v-card-text>
        <v-data-table
          :headers="headers"
          :items="messages"
          :loading="loading"
          :search="search"
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
        { text: '#', value: 'id' },
        { text: 'Event Action Code', value: 'eventActionCode' },
        { text: 'Event ID Code', value: 'eventId' },
        { text: 'Event Type Codes', value: 'eventTypes' },
        { text: 'Event Date Time', value: 'eventDateTime' },
        { text: 'Event Outcome Indicator', value: 'eventOutcomeIndicator' },
        { value: 'actions', sortable: false }
      ],
      messages: [],
      search: '',
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
    axios
      .get(process.env.VUE_APP_SERVER_URL + '/api/atna/messages')
      .then((response) => {
        this.messages = response.data
        this.loading = false
      })
  }
}
</script>
