'use strict'

// CAUTION: Don't forget to deactivate notifyMatchDeleted first!
// Get credentials from https://console.cloud.google.com/iam-admin/serviceaccounts?project=quiznerd-49e4f&authuser=0

const admin = require('firebase-admin')

const credentials = require('./data/quiznerd-49e4f-c474d2fe3a83.json');

admin.initializeApp({
    credential: admin.credential.cert(credentials)
});

const dry = process.argv.includes('--dry')
if (dry) console.log('Running in dry mode. Don\' worry...')

let db = admin.firestore();
let matches = db.collection('matches');
let botdata = db.collection('botdata');

let countMatches = 0
let countBotdata = 0

let refDateMatches = new Date()
let refDateBotdata = new Date()
refDateMatches.setDate(refDateMatches.getDate() - 30)
refDateBotdata.setDate(refDateBotdata.getDate() - 1)

let matchPromises = [
    matches.where('active', '==', false).get(), ,
    matches.where('updated', '<=', refDateMatches).get()
]

let botdataPromises = [
    botdata.where('active', '==', false).get(), ,
    botdata.where('nextExecution', '<=', refDateBotdata).get()
]

Promise.all(matchPromises.filter(p => !!p))
    .then(snapshots => {
        let promises = snapshots.map(snapshot => {
            let batch = db.batch()
            snapshot.forEach(doc => {
                countMatches++
                if (!dry) batch.delete(doc.ref)
            })
            return batch.commit()
        })
        return Promise.all(promises)
    })
    .then(() => {
        console.log(`Successfully deleted ${countMatches} matches.`)
    })
    .catch(err => {
        console.log('Error deleting matches', err)
    })

Promise.all(botdataPromises.filter(p => !!p))
    .then(snapshots => {
        let promises = snapshots.map(snapshot => {
            let batch = db.batch()
            snapshot.forEach(doc => {
                countBotdata++
                if (!dry) batch.delete(doc.ref)
            })
            return batch.commit()
        })
        return Promise.all(promises)
    })
    .then(() => {
        console.log(`Successfully deleted ${countBotdata} botdata documents.`)
    })
    .catch(err => {
        console.log('Error deleting botdata', err)
    })
