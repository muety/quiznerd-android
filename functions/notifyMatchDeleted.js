'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');

exports.fn = functions.firestore
    .document('matches/{matchId}')
    .onDelete((snap, context) => {
        let value = snap.data();
        let title = 'Quiz deleted';
        let promises = [
            admin.messaging().sendToTopic(value.player1.id, {
                notification: {
                    title: title,
                    body: `A quiz match against ${value.player2.id} was deleted.`
                }
            }),
            admin.messaging().sendToTopic(value.player2.id, {
                notification: {
                    title: title,
                    body: `A quiz match against ${value.player1.id} was deleted.`
                }
            })
        ];
        return Promise.all(promises);
    });