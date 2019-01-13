'use strict';

// https://firebase.google.com/docs/functions/firestore-events
// firebase deploy --only functions --project quiznerd

const admin = require('firebase-admin');
admin.initializeApp();

exports.poke = require('./poke').fn;

exports.notifyNewMatch = require('./notifyNewMatch').fn;

exports.notifyMatchDeleted = require('./notifyMatchDeleted').fn;;

exports.notifyMatchStateChange = require('./notifyMatchStateChange').fn;