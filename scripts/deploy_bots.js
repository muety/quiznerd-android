'use strict'

// Add questions from JSON (stored in data/new) according to template data/question.json.tpl
// Each question has to be a separate file

// Get credentials from https://console.cloud.google.com/iam-admin/serviceaccounts?project=quiznerd-49e4f&authuser=0

const admin = require('firebase-admin');

const credentials = require('./data/quiznerd-49e4f-firebase-adminsdk-3kssd-3b1663ae73.json')

admin.initializeApp({
    credential: admin.credential.cert(credentials)
});

let users = admin.firestore().collection('users')

let bots = require('../functions/botConfig.json')

bots.forEach(b => {
    users.doc(b.id).set({
        authentication: b.authentication,
        gender: b.gender
    });
});
