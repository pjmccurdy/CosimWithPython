package org.webgme.guest.randnumgen;

import org.webgme.guest.randnumgen.rti.*;

import org.cpswt.config.FederateConfig;
import org.cpswt.config.FederateConfigParser;
import org.cpswt.hla.InteractionRoot;
import org.cpswt.hla.base.AdvanceTimeRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.cpswt.utils.CpswtUtils;

// Define the RandNumGen type of federate for the federation.

public class RandNumGen extends RandNumGenBase {
    private final static Logger log = LogManager.getLogger();

    private double currentTime = 0;

    public RandNumGen(FederateConfig params) throws Exception {
        super(params);
    }

    private void checkReceivedSubscriptions() {
        InteractionRoot interaction = null;
        while ((interaction = getNextInteractionNoWait()) != null) {
            if (interaction instanceof SendNum) {
                handleInteractionClass((SendNum) interaction);
            }
            else {
                log.debug("unhandled interaction: {}", interaction.getClassName());
            }
        }
    }
    
    // Initializing Global Parameters
    int num1, num2;
    String snum1, snum2;
    String separator = ",";
    boolean receivedSimTime=false;
    String dataString="";

    private void execute() throws Exception {
        if(super.isLateJoiner()) {
            log.info("turning off time regulation (late joiner)");
            currentTime = super.getLBTS() - super.getLookAhead();
            super.disableTimeRegulation();
        }

        /////////////////////////////////////////////
        // TODO perform basic initialization below //
        /////////////////////////////////////////////

        AdvanceTimeRequest atr = new AdvanceTimeRequest(currentTime);
        putAdvanceTimeRequest(atr);

        if(!super.isLateJoiner()) {
            log.info("waiting on readyToPopulate...");
            readyToPopulate();
            log.info("...synchronized on readyToPopulate");
        }

        ///////////////////////////////////////////////////////////////////////
        // TODO perform initialization that depends on other federates below //
        ///////////////////////////////////////////////////////////////////////

        if(!super.isLateJoiner()) {
            log.info("waiting on readyToRun...");
            readyToRun();
            log.info("...synchronized on readyToRun");
        }

        startAdvanceTimeThread();
        log.info("started logical time progression");

        while (!exitCondition) {
            atr.requestSyncStart();
            enteredTimeGrantedState();

            ////////////////////////////////////////////////////////////
            // TODO send interactions that must be sent every logical //
            // time step below                                        //
            ////////////////////////////////////////////////////////////

            // Set the interaction's parameters.
            //
            //    ReceiveNum vReceiveNum = create_ReceiveNum();
            //    vReceiveNum.set_actualLogicalGenerationTime( < YOUR VALUE HERE > );
            //    vReceiveNum.set_dataString( < YOUR VALUE HERE > );
            //    vReceiveNum.set_federateFilter( < YOUR VALUE HERE > );
            //    vReceiveNum.set_originFed( < YOUR VALUE HERE > );
            //    vReceiveNum.set_sourceFed( < YOUR VALUE HERE > );
            //    vReceiveNum.sendInteraction(getLRC(), currentTime + getLookAhead());

            num1 = (int)(Math.random()*10);
            num2 = (int)(Math.random()*10);
            snum1 = String.valueOf(num1);
            snum2 = String.valueOf(num2);
            
            dataString = snum1 +separator + snum2;
            
            ReceiveNum vReceiveNum = create_ReceiveNum();
            vReceiveNum.set_dataString(dataString);
            log.info("Sent receiveNum interaction with {}",  dataString);
            
            vReceiveNum.sendInteraction(getLRC());

            // removing time delay...
            while (!receivedSimTime){
                log.info("waiting to receive SimTime...");
                synchronized(lrc){
                    lrc.tick();
                } 
                checkReceivedSubscriptions();
                if(!receivedSimTime){
                    CpswtUtils.sleep(1000);
                }
            }
            receivedSimTime = false;
            // ...........

            System.out.println(currentTime);

            ////////////////////////////////////////////////////////////////////
            // TODO break here if ready to resign and break out of while loop //
            ////////////////////////////////////////////////////////////////////

            if (!exitCondition) {
                currentTime += super.getStepSize();
                AdvanceTimeRequest newATR =
                    new AdvanceTimeRequest(currentTime);
                putAdvanceTimeRequest(newATR);
                atr.requestSyncEnd();
                atr = newATR;
            }
        }

        // call exitGracefully to shut down federate
        exitGracefully();

        //////////////////////////////////////////////////////////////////////
        // TODO Perform whatever cleanups are needed before exiting the app //
        //////////////////////////////////////////////////////////////////////
    }

    private void handleInteractionClass(SendNum interaction) {
        ///////////////////////////////////////////////////////////////
        // TODO implement how to handle reception of the interaction //
        ///////////////////////////////////////////////////////////////
        String holder = null;
    	holder = interaction.get_dataString();
    	System.out.println("holder received as: " + holder);
    	
    	String vars[] = holder.split(separator);
    	System.out.println("vars[0]=" + vars[0]);
        receivedSimTime=true;
    }

    public static void main(String[] args) {
        try {
            FederateConfigParser federateConfigParser =
                new FederateConfigParser();
            FederateConfig federateConfig =
                federateConfigParser.parseArgs(args, FederateConfig.class);
            RandNumGen federate =
                new RandNumGen(federateConfig);
            federate.execute();
            log.info("Done.");
            System.exit(0);
        }
        catch (Exception e) {
            log.error(e);
            System.exit(1);
        }
    }
}
