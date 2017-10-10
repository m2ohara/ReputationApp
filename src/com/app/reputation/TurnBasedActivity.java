package com.app.reputation;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.service.carrier.CarrierMessagingService.ResultCallback;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.InitiateMatchResult;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.UpdateMatchResult;

public class TurnBasedActivity extends Activity
implements ConnectionCallbacks, OnConnectionFailedListener,
OnInvitationReceivedListener, OnTurnBasedMatchUpdateReceivedListener,
OnClickListener {
	
	public static final String TAG = "TurnBasedActivity";
	private GoogleApiClient googleApiClient;
	
	private static final int RC_SIGN_IN = 9001;
	
	private TurnBasedMatch mTurnBasedMatch;	
    public boolean isDoingTurn = false;
    public TurnBasedMatch mMatch;    
    public TurnData mTurnData;
    
    private AlertDialog mAlertDialog;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_turnbased);
    	
    	googleApiClient = new GoogleApiClient.Builder(this)
    	.addConnectionCallbacks(this)
    	.addOnConnectionFailedListener(this)
    	.addApi(Games.API).addScope(Games.SCOPE_GAMES)
    	.build();
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	Log.d(TAG, "onStart: connecting to Google APIs");
    	googleApiClient.connect();
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	if(googleApiClient.isConnected()) {
    		googleApiClient.disconnect();
    		Log.d(TAG, "onStop: disconnecting from Google APIs");
    	}
    }
    
    @Override
    public void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        
//        if (request == RC_SIGN_IN) {
//            mSignInClicked = false;
//            mResolvingConnectionFailure = false;
//            if (response == Activity.RESULT_OK) {
//                googleApiClient.connect();
//            } else {
//                BaseGameUtils.showActivityResultError(this, request, response, R.string.signin_other_error);
//            }
//        }
        
        final ArrayList<String> invitees = data
                .getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
    	
    	TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
    	        .addInvitedPlayers(invitees)
    	        .build();
    	
    	ResultCallback<InitiateMatchResult> rc = new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
			@Override
			public void onReceiveResult(InitiateMatchResult result)
			{
				processResult(result);
				
			};
			
    	};
    	
    	Games.TurnBasedMultiplayer.createMatch(googleApiClient, tbmc).setResultCallback((com.google.android.gms.common.api.ResultCallback<? super InitiateMatchResult>) rc);
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTurnBasedMatchReceived(TurnBasedMatch arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTurnBasedMatchRemoved(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInvitationReceived(Invitation arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInvitationRemoved(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected(): Connection successful");

        // Retrieve the TurnBasedMatch from the connectionHint
        if (connectionHint != null) {
            mTurnBasedMatch = connectionHint.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);

            if (mTurnBasedMatch != null) {
                if (googleApiClient == null || !googleApiClient.isConnected()) {
                    Log.d(TAG, "Warning: accessing TurnBasedMatch when not connected");
                }

                updateMatch(mTurnBasedMatch);
                return;
            }
        }
		
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		
	}
	
    public void setViewVisibility() {
        boolean isSignedIn = (googleApiClient != null) && (googleApiClient.isConnected());

        if (!isSignedIn) {
//            findViewById(R.id.login_layout).setVisibility(View.VISIBLE);
//            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.matchup_layout).setVisibility(View.GONE);
            findViewById(R.id.gameplay_layout).setVisibility(View.GONE);

            if (mAlertDialog != null) {
                mAlertDialog.dismiss();
            }
            return;
        }


//        ((TextView) findViewById(R.id.name_field)).setText(Games.Players.getCurrentPlayer(
//                googleApiClient).getDisplayName());
//        findViewById(R.id.login_layout).setVisibility(View.GONE);

        if (isDoingTurn) {
            findViewById(R.id.matchup_layout).setVisibility(View.GONE);
            findViewById(R.id.gameplay_layout).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.matchup_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.gameplay_layout).setVisibility(View.GONE);
        }
    }
	
    public void setGameplayUI() {
        isDoingTurn = true;
        setViewVisibility();
//        mDataView.setText(mTurnData.data);
//        mTurnTextView.setText("Turn " + mTurnData.turnCounter);
    }
	
    public void updateMatch(TurnBasedMatch match) {
        mMatch = match;

        int status = match.getStatus();
        int turnStatus = match.getTurnStatus();

        switch (status) {
            case TurnBasedMatch.MATCH_STATUS_CANCELED:
                //showWarning("Canceled!", "This game was canceled!");
                return;
            case TurnBasedMatch.MATCH_STATUS_EXPIRED:
                //showWarning("Expired!", "This game is expired.  So sad!");
                return;
            case TurnBasedMatch.MATCH_STATUS_AUTO_MATCHING:
                //showWarning("Waiting for auto-match...",
//                        "We're still waiting for an automatch partner.");
                return;
            case TurnBasedMatch.MATCH_STATUS_COMPLETE:
                if (turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE) {
//                    showWarning(
//                            "Complete!",
//                            "This game is over; someone finished it, and so did you!  There is nothing to be done.");
                    break;
                }

                // Note that in this state, you must still call "Finish" yourself,
                // so we allow this to continue.
//                showWarning("Complete!",
//                        "This game is over; someone finished it!  You can only finish it now.");
        }

        // OK, it's active. Check on turn status.
        switch (turnStatus) {
            case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
                mTurnData = TurnData.unpersist(mMatch.getData());
                setGameplayUI();
                return;
            case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
                // Should return results.
//                showWarning("Alas...", "It's not your turn.");
                break;
            case TurnBasedMatch.MATCH_TURN_STATUS_INVITED:
//                showWarning("Good inititative!",
//                        "Still waiting for invitations.\n\nBe patient!");
        }

        mTurnData = null;

        setViewVisibility();
    }
    
    
    private void processResult(TurnBasedMultiplayer.InitiateMatchResult result) {
        TurnBasedMatch match = result.getMatch();

//        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
//            return;
//        }

        if (match.getData() != null) {
            // This is a game that has already started, so I'll just start
            updateMatch(match);
            return;
        }

        startMatch(match);
    }
    

    public void processResult(TurnBasedMultiplayer.UpdateMatchResult result) {
        TurnBasedMatch match = result.getMatch();
//        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
//            return;
//        }

        isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);

        if (isDoingTurn) {
            updateMatch(match);
            return;
        }

        setViewVisibility();
    }
    
    // startMatch() happens in response to the createTurnBasedMatch()
    // above. This is only called on success, so we should have a
    // valid match object. We're taking this opportunity to setup the
    // game, saving our initial state. Calling takeTurn() will
    // callback to OnTurnBasedMatchUpdated(), which will show the game
    // UI.
    public void startMatch(TurnBasedMatch match) {
        mTurnData = new TurnData();
        // Some basic turn data
        mTurnData.data = "First turn";

        mMatch = match;

        String playerId = Games.Players.getCurrentPlayerId(googleApiClient);
        String myParticipantId = mMatch.getParticipantId(playerId);
        
        ResultCallback<TurnBasedMultiplayer.UpdateMatchResult> rc = new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {

			@Override
			public void onReceiveResult(TurnBasedMultiplayer.UpdateMatchResult result)
					throws RemoteException {
				processResult(result);
			}
        };

        Games.TurnBasedMultiplayer.takeTurn(googleApiClient, match.getMatchId(),
                mTurnData.persist(), myParticipantId).setResultCallback((com.google.android.gms.common.api.ResultCallback<? super UpdateMatchResult>) rc);
    }
    
//    public void showWarning(String title, String message) {
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//
//        // set title
//        alertDialogBuilder.setTitle(title).setMessage(message);
//
//        // set dialog message
//        alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        // if this button is clicked, close
//                        // current activity
//                    }
//                });
//
//        // create alert dialog
//        mAlertDialog = alertDialogBuilder.create();
//
//        // show it
//        mAlertDialog.show();
//    }

}
