package com.app.reputation;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
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
implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
OnInvitationReceivedListener, OnTurnBasedMatchUpdateReceivedListener,
View.OnClickListener {
	
	public static final String TAG = "TurnBasedActivity";
	private GoogleApiClient googleApiClient;
	
	private static final int RC_SIGN_IN = 9001;
	final static int RC_SELECT_PLAYERS = 1000;
	final static int RC_LOOK_AT_MATCHES = 10001;
	
	private boolean signInClicked = false;
	
	private TurnBasedMatch turnBasedMatch;	
    public boolean isDoingTurn = false;
    public TurnBasedMatch mMatch;    
    public TurnData mTurnData;
    
    private AlertDialog mAlertDialog;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_turnbased);
    	
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
    	
    	googleApiClient = new GoogleApiClient.Builder(this)
    	.addConnectionCallbacks(this)
    	.addOnConnectionFailedListener(this)
    	.addApi(Games.API).addScope(Games.SCOPE_GAMES)
    	.build();
    	
    	createGrid(); 
    	
    	setContactView();
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
	public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected(): Connection successful");
        // show sign-out button, hide the sign-in button
        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
        
        if (connectionHint == null) {
        	//onStartMatchInitiated(); 
        	//onCheckGamesClicked();
//        	createMatch();
        }

        // Retrieve the TurnBasedMatch from the connectionHint
        if (connectionHint != null) {
            turnBasedMatch = connectionHint.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);

            if (turnBasedMatch != null) {
                if (googleApiClient == null || !googleApiClient.isConnected()) {
                    Log.d(TAG, "Warning: accessing TurnBasedMatch when not connected");
                }

                updateMatch(turnBasedMatch);
                return;
            }
        }
		
	}
    
	@Override
	public void onClick(View v) {
        switch (v.getId()) {
        case R.id.sign_in_button:
            // Check to see the developer who's running this sample code read the instructions :-)
            // NOTE: this check is here only because this is a sample! Don't include this
            // check in your actual production app.
//            if (!BaseGameUtils.verifySampleSetup(this, R.string.app_id)) {
//                Log.w(TAG, "*** Warning: setup problems detected. Sign in may not work!");
//            }

            signInClicked = true;
            turnBasedMatch = null;
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            googleApiClient.connect();
            break;
        case R.id.sign_out_button:
            signInClicked = false;
            Games.signOut(googleApiClient);
            if (googleApiClient.isConnected()) {
                googleApiClient.disconnect();
            }
            setViewVisibility();
            break;
    }
	}
	
    public void onCheckGamesClicked() {
        Intent intent = 
        		Games.TurnBasedMultiplayer.getInboxIntent(googleApiClient);
        startActivityForResult(intent, RC_LOOK_AT_MATCHES);
    }
    
    public void onStartMatchInitiated() {
    	
        Intent intent =
            Games.TurnBasedMultiplayer.getSelectOpponentsIntent(googleApiClient, 1, 7, true);
        startActivityForResult(intent, RC_SELECT_PLAYERS);
        
    }
    
    public void createMatch() {
        final ArrayList<String> invitees = new ArrayList<String>( Arrays.asList("g05776219623626509277") );
    	
    	TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
    	        .addInvitedPlayers(invitees)
    	        .build();
    	
    	ResultCallback<InitiateMatchResult> rc = new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
			@Override
			public void onResult(InitiateMatchResult result)
			{
				processResult(result);
				
			};
			
    	};
    	
    	Games.TurnBasedMultiplayer.createMatch(googleApiClient, tbmc).setResultCallback(rc);
    }
    
    @Override
    public void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        
        if (request == RC_SIGN_IN) {
            signInClicked = false;
//            mResolvingConnectionFailure = false;
            if (response == Activity.RESULT_OK) {
                googleApiClient.connect();
            } 
            else {
//                BaseGameUtils.showActivityResultError(this, request, response, R.string.signin_other_error);
            }
        }
        else if (request == RC_LOOK_AT_MATCHES) {
            // Returning from the 'Select Match' dialog

            if (response != Activity.RESULT_OK) {
                // user canceled
                return;
            }

            TurnBasedMatch match = data
                    .getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);

            if (match != null) {
                updateMatch(match);
            }

            Log.d(TAG, "Match = " + match);
        }
        else if (request == RC_SELECT_PLAYERS) {
        	

            if (response != Activity.RESULT_OK) {
                // user canceled
                return;
            }
        
	        final ArrayList<String> invitees = data
	                .getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
	    	
	    	TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
	    	        .addInvitedPlayers(invitees)
	    	        .build();
	    	
	    	ResultCallback<InitiateMatchResult> rc = new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
				@Override
				public void onResult(InitiateMatchResult result)
				{
					processResult(result);
					
				};
				
	    	};
	    	
	    	Games.TurnBasedMultiplayer.createMatch(googleApiClient, tbmc).setResultCallback(rc);
        }
    }
	
    public void setViewVisibility() {
        boolean isSignedIn = (googleApiClient != null) && (googleApiClient.isConnected());

        if (!isSignedIn) {
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
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
    }
    

    public void startMatch(TurnBasedMatch match, String emojiId) {
        mTurnData = new TurnData();
        // Some basic turn data
        mTurnData.data = emojiId;

        mMatch = match;

        String playerId = Games.Players.getCurrentPlayerId(googleApiClient); //m2.ohara g11950741936762931340, causal.labs g05776219623626509277
        String myParticipantId = mMatch.getParticipantId(playerId);
        
        ResultCallback<TurnBasedMultiplayer.UpdateMatchResult> rc = new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {

			@Override
			public void onResult(UpdateMatchResult result) {
				processResult(result);
				
			}
        };

        Games.TurnBasedMultiplayer.takeTurn(googleApiClient, match.getMatchId(),
                mTurnData.persist(), myParticipantId).setResultCallback(rc);
    }
	
    public void updateMatch(TurnBasedMatch match) {
        mMatch = match;

        int status = match.getStatus();
        int turnStatus = match.getTurnStatus();

        switch (status) {
            case TurnBasedMatch.MATCH_STATUS_EXPIRED:
                showWarning("Expired!", "The emoji has expired");
                return;
            case TurnBasedMatch.MATCH_STATUS_COMPLETE:
                if (turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE) {
                    showWarning(
                            "Sent!",
                            "You've already sent an emoji");
                    break;
                }

                // Note that in this state, you must still call "Finish" yourself,
                // so we allow this to continue.
                showWarning("Emoji sent",
                        "You've already sent an emoji");
        }

        // OK, it's active. Check on turn status.
        switch (turnStatus) {
            case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
                mTurnData = TurnData.unpersist(mMatch.getData());
                setGameplayUI();
                return;
//            case TurnBasedMatch.MATCH_TURN_STATUS_INVITED:
//                Intent intent = Games.TurnBasedMultiplayer.acceptInvitation(googleApiClient, arg1)
            case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
                // Should return results.
                showWarning("Alas...", "It's not your turn.");
                break;
        }

        mTurnData = null;

        setViewVisibility();
    }
    
    
    private void processResult(TurnBasedMultiplayer.InitiateMatchResult result) {
        TurnBasedMatch match = result.getMatch();

        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
            return;
        }

        if (match.getData() != null) {
            // This is a game that has already started, so I'll just start
            updateMatch(match);
            return;
        }
        
        mMatch = result.getMatch();
    }
    

    public void processResult(TurnBasedMultiplayer.UpdateMatchResult result) {
        TurnBasedMatch match = result.getMatch();
        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
            return;
        }

        isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);

        if (isDoingTurn) {
            updateMatch(match);
            return;
        }

        setViewVisibility();
    }
    
    public void createGrid() {
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v,
					int position, long id) {
				
                // Create a new ClipData.Item from the ImageView object's tag
                ClipData.Item item = new ClipData.Item(Integer.toString(position));

                ClipData dragData = new ClipData(Integer.toString(position), new String[] {ClipDescription.MIMETYPE_TEXT_PLAIN}, item);

                // Instantiates the drag shadow builder.
                View.DragShadowBuilder shadow = new DragShadowBuilder(v);

                // Starts the drag

                v.startDrag(dragData,  // the data to be dragged
                		shadow,  // the drag shadow builder
                		null,      // no need to use local data
                		0          // flags (not currently used, set to 0)
                		);
                
                return false;
			}});

    }
    
    public void setContactView() {
    	
    	View contactView = findViewById(R.id.imageView1);
    	
    	contactView.setOnDragListener(new DragEventListener(this));
    }
    
    public void onDragDrop(ClipData.Item item) {
    	
    	startMatch(mMatch, item.getText().toString());
    }
    
    //********************************* Message display*//
    
    // Generic warning/info dialog
    public void showWarning(String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(title).setMessage(message);

        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                    }
                });

        // create alert dialog
        mAlertDialog = alertDialogBuilder.create();

        // show it
        mAlertDialog.show();
    }
    
    public void showErrorMessage(TurnBasedMatch match, int statusCode,
            int stringId) {

        showWarning("Warning", getResources().getString(stringId));
    }

    // Returns false if something went wrong, probably. This should handle
    // more cases, and probably report more accurate results.
    private boolean checkStatusCode(TurnBasedMatch match, int statusCode) {
        switch (statusCode) {
            case GamesStatusCodes.STATUS_OK:
                return true;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_DEFERRED:
                // This is OK; the action is stored by Google Play Services and will
                // be dealt with later.
                Toast.makeText(
                        this,
                        "Stored action for later.  (Please remove this toast before release.)",
                        Toast.LENGTH_SHORT).show();
                // NOTE: This toast is for informative reasons only; please remove
                // it from your final application.
                return true;
            case GamesStatusCodes.STATUS_MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
                showErrorMessage(match, statusCode,
                        R.string.status_multiplayer_error_not_trusted_tester);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_ALREADY_REMATCHED:
                showErrorMessage(match, statusCode,
                        R.string.match_error_already_rematched);
                break;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_FAILED:
                showErrorMessage(match, statusCode,
                        R.string.network_error_operation_failed);
                break;
            case GamesStatusCodes.STATUS_CLIENT_RECONNECT_REQUIRED:
                showErrorMessage(match, statusCode,
                        R.string.client_reconnect_required);
                break;
            case GamesStatusCodes.STATUS_INTERNAL_ERROR:
                showErrorMessage(match, statusCode, R.string.internal_error);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_INACTIVE_MATCH:
                showErrorMessage(match, statusCode,
                        R.string.match_error_inactive_match);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_LOCALLY_MODIFIED:
                showErrorMessage(match, statusCode,
                        R.string.match_error_locally_modified);
                break;
            default:
                showErrorMessage(match, statusCode, R.string.unexpected_status);
                Log.d(TAG, "Did not have warning or string to deal with: "
                        + statusCode);
        }

        return false;
    }
    
	@Override
	public void onInvitationReceived(Invitation invitation) {
		Games.TurnBasedMultiplayer.acceptInvitation(googleApiClient, invitation.getInvitationId());
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
	public void onInvitationRemoved(String arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		
		Log.d(TAG, "Error: connection failed: "+arg0);
	}

}
