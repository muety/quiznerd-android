'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');

exports.fn = functions.firestore
    .document('matches/{matchId}')
    .onCreate((snap, context) => {
        let value = snap.data();
        const payload = {
            notification: {
                title: 'New quiz match!',
                body: `${value.player1.id} wants to challenge you.`
            }
        };
        return admin.messaging().sendToTopic(value.player2.id, payload);
    });