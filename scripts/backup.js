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
let users = db.collection('users');

let data = {
    matches: {},
    users: {}
}

let lastUpdate = new Date();

matches.get()
    .then(snapshot => {
        snapshot.forEach(doc => {
            console.log(`Match ${doc.id}`);
            data.matches[doc.id] = doc.data();
            lastUpdate = new Date();
        })
    })
    .catch(err => {
        console.log('Error getting documents', err);
    });

users.get()
    .then(snapshot => {
        snapshot.forEach(doc => {
            console.log(`User ${doc.id}`);
            data.users[doc.id] = doc.data();
            lastUpdate = new Date();
        })
    })
    .catch(err => {
        console.log('Error getting documents', err);
    });

setInterval(() => {
    if (new Date() - lastUpdate > 10 * 1000) {
        fs.writeFileSync(`data/backup_matches_${new Date().toISOString()}.json`, JSON.stringify(data.matches, null, 4));
        fs.writeFileSync(`data/backup_users_${new Date().toISOString()}.json`, JSON.stringify(data.users, null, 4));
        process.exit(0);
    }
}, 1000);