'use strict';

// https://firebase.google.com/docs/functions/firestore-events
// firebase deploy --only functions --project quiznerd

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.notifyNewMatch = functions.firestore
  .document('matches/{matchId}')
  .onCreate((snap, context) => {
    const payload = {
      notification: {
        title: 'New quiz match!',
        body: `${snap.data().player1.id} wants to challenge you.`
      }
    };
    return admin.messaging().sendToTopic(snap.data().player2.id, payload);
  });

exports.notifyMatchFinished = functions.firestore
  .document('matches/{matchId}')
  .onUpdate((change, context) => {
    const oldVal = change.before.data();
    const newVal = change.after.data();

    if (!oldVal.active || newVal.active) return null;

    let winner = null;
    let loser = null;

    let scores = getPlayerScores(newVal);
    if (scores[0] > scores[1]) {
      winner = newVal.player1;
      loser = newVal.player2;
    } else if (scores[0] < scores[1]) {
      winner = newVal.player2;
      loser = newVal.player1;
    }

    if (winner && loser) {
      let p1 = admin.messaging().sendToTopic(winner.id, {
        notification: {
          title: `You won against ${loser.id}`,
          body: `You just won your quiz match against ${loser.id}, congratulations!`
        }
      });

      let p2 = admin.messaging().sendToTopic(loser.id, {
        notification: {
          title: `You lost against ${winner.id}`,
          body: `Sorry, you lost your quiz match against ${winner.id}.`
        }
      });
      return Promise.all([p1, p2]);
    } else {
      let p1 = admin.messaging().sendToTopic(newVal.player1.id, {
        notification: {
          title: `Draw against ${newVal.player2.id}`,
          body: `Your quiz match against ${newVal.player2.id} was a draw.`
        }
      });
      let p2 = admin.messaging().sendToTopic(newVal.player2.id, {
        notification: {
          title: `Draw against ${newVal.player1.id}`,
          body: `Your quiz match against ${newVal.player1.id} was a draw.`
        }
      });
      return Promise.all([p1, p2]);
    }
  });

function getCorrectAnswer(question) {
  return question.answers.filter(a => a.correct)[0];
}

function getPlayerScores(match) {
  let player1Score = 0;
  let player2Score = 0;

  for (let i = 0; i < match.rounds.length; i++) {
    let round = match.rounds[i];
    for (let j = 0; j < round.questions.length; j++) {
      let question = round.questions[j];
      let correct = getCorrectAnswer(question);
      if (round.answers1[j] == correct.id) player1Score++;
      if (round.answers2[j] == correct.id) player2Score++;
    }
  }

  return [player1Score, player2Score];
}