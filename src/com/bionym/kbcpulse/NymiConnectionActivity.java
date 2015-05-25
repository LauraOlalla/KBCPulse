package com.bionym.kbcpulse;

import java.util.Arrays;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bionym.ncl.Ncl;
import com.bionym.ncl.NclCallback;
import com.bionym.ncl.NclEvent;
import com.bionym.ncl.NclEventInit;
import com.bionym.ncl.NclMode;
import com.bionym.ncl.NclProvision;

public class NymiConnectionActivity extends Activity implements ProvisionController.ProvisionProcessListener, ValidationController.ValidationProcessListener
                                                 {
	static final String LOG_TAG = "AndroidExample";

    static boolean nclInitialized = false;
    
    String nymulatorIp = "10.0.2.2";
    Button startProvision, startValidation;

    ProvisionController provisionController;
    ValidationController valiationController;

    boolean connectNymi = false; //true: nymi band, false: emulator

    int nymiHandle = Ncl.NYMI_HANDLE_ANY;
    NclProvision provision;

//    static Pattern ipPattern = Pattern.compile("^\\s*\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\s*$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nymiconnection);
    }

    @Override
    protected void onStart() {
        super.onStart();

        startProvision = (Button) findViewById(R.id.provision);
        startValidation = (Button) findViewById(R.id.validation);

        startProvision.setOnClickListener(new View.OnClickListener() {
        		long lastClickTime; // for double click protection
            @Override
            public void onClick(View v) {
            		if (System.currentTimeMillis() - lastClickTime >= 1000) { // double click protection
            			lastClickTime = System.currentTimeMillis();
	                initializeNcl();
	                nymiHandle = -1;
	                if (provisionController == null) {
	                		provisionController = new ProvisionController(NymiConnectionActivity.this);
	                }
	                else {
	                		provisionController.stop();
	                }
	                provisionController.startProvision(NymiConnectionActivity.this);
            		}
            }
        });
        
        startValidation.setOnClickListener(new View.OnClickListener() {
    		long lastClickTime;
        @Override
        public void onClick(View v) {
        		if (System.currentTimeMillis() - lastClickTime >= 1000) { // double click protection
        			lastClickTime = System.currentTimeMillis();
//        			startProvision.setEnabled(false);
        			if (valiationController == null) {
        				valiationController = new ValidationController(NymiConnectionActivity.this);
        			}
        			else {
        				valiationController.stop();
        			}
                valiationController.startValidation(NymiConnectionActivity.this, provisionController.getProvision());
        		}
        }
    });
    }

   
    @Override
	protected void onPause() {
	    	if (provisionController != null) {
	    		provisionController.stop();
	    	}
	    	
	    	if (valiationController != null) {
	    		valiationController.stop();
	    	}
		super.onPause();
	}

	@Override
    protected void onStop() {
        if (nclInitialized && nymiHandle >= 0) {
            Ncl.disconnect(nymiHandle);
        }

        super.onStop();
    }

    /**
     * Initialize the NCL library
     */
    protected void initializeNcl() {
        if (!nclInitialized) {
            if (connectNymi) {
                initializeNclForNymiBand();
            }
            else {
//                initializeNclForNymulator(nymulatorIp.getText().toString().trim());
                initializeNclForNymulator(nymulatorIp);
            }
        }
    }

    /**
     * Process view when NCL library is initialized
     */
//    protected void nclInitialized() {
//        View selectLibraryContainer = findViewById(R.id.selectLibContainer);
//        selectLibraryContainer.setVisibility(View.GONE);
//    }

    /**
     * Initialize NCL library for connecting to a Nymi Band
     * @return true if the library is initialized
     */
    protected boolean initializeNclForNymiBand() {
        if (!nclInitialized) {
	    		NclCallback nclCallback = new MyNclCallback();
            boolean result = Ncl.init(nclCallback, null, "NCLExample", NclMode.NCL_MODE_DEFAULT, this);

            if (!result) { // failed to initialize NCL
                Toast.makeText(NymiConnectionActivity.this, "Failed to initialize NCL library!", Toast.LENGTH_LONG).show();
                return false;
            }
            nclInitialized = true;
//            nclInitialized();
        }
        return true;
    }

    /**
     * Initialize NCL library for connecting to a Nymulator
     * @param ip the Nymulator's IP address
     * @return true if the library is initialized
     */
    protected boolean initializeNclForNymulator(String ip) {
        if (!nclInitialized) {
            NclCallback nclCallback = new MyNclCallback();
            Ncl.setIpAndPort(ip, 9089);
            boolean result = Ncl.init(nclCallback, null, "KBCPulse", NclMode.NCL_MODE_DEFAULT, this);

            if (!result) { // failed to initialize NCL
                Toast.makeText(NymiConnectionActivity.this, "Failed to initialize NCL library!", Toast.LENGTH_LONG).show();
                return false;
            }

            nclInitialized = true;
//            nclInitialized();
        }

        return true;
    }
	
    @Override
    public void onStartProcess(ProvisionController controller) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NymiConnectionActivity.this, "Nymi start provision ..",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onAgreement(final ProvisionController controller) {
        nymiHandle = controller.getNymiHandle();
        controller.accept();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NymiConnectionActivity.this, "Agree on pattern: " + Arrays.toString(controller.getLedPatterns()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onProvisioned(final ProvisionController controller) {
    		nymiHandle = controller.getNymiHandle();
        provision = controller.getProvision();
		controller.stop();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
	        		startProvision.setEnabled(false);	     
//	                Toast.makeText(ProvisioningActivity.this, "Nymi provisioned: " + Arrays.toString(provision.id.v),
//	                        Toast.LENGTH_LONG).show();
            	
	                Toast.makeText(NymiConnectionActivity.this, "Connection has been stablished correctly.",
	                Toast.LENGTH_LONG).show(); 	
	                
	                //Show text an bottom for the validation
	                TextView validationText = (TextView) findViewById(R.id.validationText);
	                validationText.setVisibility(View.VISIBLE);
	                startValidation.setVisibility(View.VISIBLE);
	                startValidation.setEnabled(true);
	                
            }
        });
    }

    @Override
    public void onFailure(ProvisionController controller) {
		controller.stop();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NymiConnectionActivity.this, "Nymi provision failed!",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    

    @Override
    public void onDisconnected(ProvisionController controller) {
		controller.stop();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	;
//	        		startValidation.setEnabled(provision != null);
//	        		disconnect.setEnabled(false);
//                Toast.makeText(MainActivity.this, "Nymi disconnected: " + provision,
//                        Toast.LENGTH_LONG).show();
            }
        });
    }
    
    @Override
    public void onStartProcess(ValidationController controller) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NymiConnectionActivity.this, "Nymi start validation for: " + Arrays.toString(provision.id.v),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onFound(ValidationController controller) {
        nymiHandle = controller.getNymiHandle();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NymiConnectionActivity.this, "Nymi validation found Nymi on: " + Arrays.toString(provision.id.v),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onValidated(ValidationController controller) {
		nymiHandle = controller.getNymiHandle();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            		startValidation.setEnabled(false);
//            		disconnect.setEnabled(true);
	            Toast.makeText(NymiConnectionActivity.this, "Nymi validated!You can already use KPCPulse",
	                    Toast.LENGTH_LONG).show();
	            
	            
	            //TODO Activity with the cards
	            
            }
        });
    }

    @Override
    public void onFailure(ValidationController controller) {
		controller.stop();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NymiConnectionActivity.this, "Nymi validated failed!",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDisconnected(ValidationController controller) {
		controller.stop();
    		runOnUiThread(new Runnable() {
            @Override
            public void run() {
//            		disconnect.setEnabled(false);
//                startValidation.setEnabled(true);
//                startProvision.setEnabled(true);
                Toast.makeText(NymiConnectionActivity.this, "Nymi disconnected: " + provision,
                        Toast.LENGTH_LONG).show();
            }
        });
    }  
    
    
    
	/**
	 * Callback for NclEventInit
	 *
	 */
	class MyNclCallback implements NclCallback {
		@Override
		public void call(NclEvent event, Object userData) {
			Log.d(LOG_TAG, this.toString() + ": " + event.getClass().getName());
			if (event instanceof NclEventInit) {
	            if (!((NclEventInit) event).success) {
	                runOnUiThread(new Runnable() {
	                    @Override
	                    public void run() {
	                        Toast.makeText(NymiConnectionActivity.this, "Failed to initialize NCL library!", Toast.LENGTH_LONG).show();
	                    }
	                });
	            }
	        }
		}
	}
}
