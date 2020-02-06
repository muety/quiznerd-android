'use strict'

// Get credentials from https://console.cloud.google.com/iam-admin/serviceaccounts?project=quiznerd-49e4f&authuser=0

const admin = require('firebase-admin'),
    fs = require('fs');

const credentials = require('./data/quiznerd-49e4f-b97ae890548f.json');

admin.initializeApp({
    credential: admin.credential.cert(credentials)
});

let db = admin.firestore();
let matches = db.collection('matches');
let botdata = db.collection('botdata');

let count1 = 0
let count2 = 0

matches.where('active', '==', false)
    .get()
    .then(snapshot => {
        let batch = db.batch()
        snapshot.forEach(doc => {
            count1++
            batch.delete(doc.ref)
        })
        return batch.commit()
    })
    .then(() => {
        console.log(`Successfully deleted ${count1} matches.`)
    })
    .catch(err => {
        console.log('Error getting matches', err);
    });

botdata.where('active', '==', false)
    .get()
    .then(snapshot => {
        let batch = db.batch()
        snapshot.forEach(doc => {
            count2++
            batch.delete(doc.ref)
        })
        return batch.commit()
    })
    .then(() => {
        console.log(`Successfully deleted ${count2} botdata documents.`)
    })
    .catch(err => {
        console.log('Error getting botdata documents', err);
    });