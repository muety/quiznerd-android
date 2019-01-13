'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');

exports.fn = functions.https.onCall((data, context) => {
    const payload = {
        notification: {
            title: 'You got poked!',
            body: `${data.playerId} wants you to play your ${data.category} match.`
        }
    };
    return admin.messaging().sendToTopic(data.opponentId, payload);
});