'use strict'

// Get credentials from https://console.cloud.google.com/iam-admin/serviceaccounts?project=quiznerd-49e4f&authuser=0

const admin = require('firebase-admin');

const credentials = require('./data/quiznerd-49e4f-3092574cdad8.json');

admin.initializeApp({
    credential: admin.credential.cert(credentials)
});

let db = admin.firestore();
let questions = db.collection('questions');
let catCount = {};

questions.get()
    .then(snapshot => {
        snapshot.forEach(q => {
            let data = q.data();
            if (!catCount.hasOwnProperty(data.category)) {
                catCount[data.category] = 0;
            }
            catCount[data.category]++;
        });

        console.log(JSON.stringify(catCount, null, 4));
    })
    .catch(err => {
        console.log('Error getting questions', err);
    });