'use strict';

// https://firebase.google.com/docs/functions/firestore-events
// firebase deploy --only functions

const admin = require('firebase-admin');
const serviceAccount = require('./quiznerd-49e4f-c474d2fe3a83.json');
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: 'https://quiznerd-49e4f.firebaseio.com'
});

// https://us-central1-quiznerd-49e4f.cloudfunctions.net/runBots
exports.runBots = require('./runBots').fn;

// https://us-central1-quiznerd-49e4f.cloudfunctions.net/poke
exports.poke = require('./poke').fn;

exports.notifyNewMatch = require('./notifyNewMatch').fn;

exports.notifyMatchDeleted = require('./notifyMatchDeleted').fn;;

exports.notifyMatchStateChange = require('./notifyMatchStateChange').fn;