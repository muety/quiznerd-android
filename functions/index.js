'use strict';

// Events: https://firebase.google.com/docs/functions/firestore-events

// Deploy:
// $ firebase deploy --only functions

// Emulator:
// $ export GOOGLE_APPLICATION_CREDENTIALS="./quiznerd-49e4f-c474d2fe3a83.json"
// $ firebase functions:config:get > .runtimeconfig.json
// $ firebase emulators:start --only functions

const admin = require('firebase-admin');
admin.initializeApp();

// https://us-central1-quiznerd-49e4f.cloudfunctions.net/runBots
exports.runBots = require('./runBots').fn;

// https://us-central1-quiznerd-49e4f.cloudfunctions.net/cleanBotdata
exports.cleanBotdata = require('./cleanBotdata').fn;

// https://us-central1-quiznerd-49e4f.cloudfunctions.net/cleanMatches
exports.cleanMatches = require('./cleanMatches').fn;

// https://us-central1-quiznerd-49e4f.cloudfunctions.net/poke
exports.poke = require('./poke').fn;

exports.notifyNewMatch = require('./notifyNewMatch').fn;

exports.notifyMatchDeleted = require('./notifyMatchDeleted').fn;;

exports.notifyMatchStateChange = require('./notifyMatchStateChange').fn;