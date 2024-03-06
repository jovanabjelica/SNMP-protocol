/*
 * Copyright (c) 2002-2003 iReasoning Inc. All Rights Reserved.
 * 
 * This SOURCE CODE FILE, which has been provided by iReasoning Inc. as part
 * of an iReasoning Software product for use ONLY by licensed users of the product,
 * includes CONFIDENTIAL and PROPRIETARY information of iReasoning Inc.  
 *
 * USE OF THIS SOFTWARE IS GOVERNED BY THE TERMS AND CONDITIONS 
 * OF THE LICENSE STATEMENT AND LIMITED WARRANTY FURNISHED WITH
 * THE PRODUCT.
 *
 * IN PARTICULAR, YOU WILL INDEMNIFY AND HOLD IREASONING SOFTWARE, ITS
 * RELATED COMPANIES AND ITS SUPPLIERS, HARMLESS FROM AND AGAINST ANY
 * CLAIMS OR LIABILITIES ARISING OUT OF THE USE, REPRODUCTION, OR
 * DISTRIBUTION OF YOUR PROGRAMS, INCLUDING ANY CLAIMS OR LIABILITIES
 * ARISING OUT OF OR RESULTING FROM THE USE, MODIFICATION, OR
 * DISTRIBUTION OF PROGRAMS OR FILES CREATED FROM, BASED ON, AND/OR
 * DERIVED FROM THIS SOURCE CODE FILE.
 */


import com.ireasoning.protocol.*;
import com.ireasoning.protocol.snmp.*;
import com.ireasoning.util.ParseArguments;
import java.io.IOException;

/**
 * This example demonstrates how to write a trapd daemon to collect SNMPv1,v2c
 * and v3 traps and informs. <br>
 * A callback interface, Listener, is implemented, so it will get notified when
 * a trap is received.
 * Note:<br>
 * Community name is not verified for each trap. So even if commnity name is not
 * correct, it's still considered as valid trap. getCommunity() can be used to
 * verify that.
 *
 * <p>
 *
 * For simplicity consideration, the SNMPv3 engine of trapd uses the same user settings as trap
 * sender.
 *
 * <pre>
 * Example: 
 * java snmptrapd 
 * java snmptrapd -v 3 -u newUser -A abc12345 -X abc12345 -l 12345 -e 456789
 *
 * Sample output:
 * 
 * Received SNMPv2/v3 trap from 127.0.0.1/127.0.0.1 
 * Community: public 
 * sysUpTime.0: 1 minute 43 seconds 
 * snmpTrapOID.0: ifAdminStatus 
 * ifAdminStatus.3: up
 *
 * </pre>
 */
public class snmptrapd extends snmp implements Listener 
{
    public static void main(String[] args)
    {
        snmptrapd s = new snmptrapd();
        s.parseOptions(args, "snmptrapd", false, true);
        s.trapd();
    }

    void trapd()
    {
        try
        {
            System.out.println( "Trap receiver listening on port: " + _port);
            SnmpTrapdSession session = new SnmpTrapdSession(_port);
            if(_isSnmpV3)
            {
                //More users can be added by calling addV3Params
                session.addV3Params(_user, _authProtocol, _authPassword, _privProtocol,
                        _privPassword, null);
            }
            
            //register for trap event, so handleMsg will be called when trap comes
            session.addListener(this);
            
            //blocks and wait for traps
            session.waitForTrap();
        }
        catch(IOException e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    /**
     * Callback method, gets called when a trap is received
     * @param msg a SnmpDataType object, more specifically, a SNMPv1/v2c/v3 trap or inform object
     * @param msgSender ignored
     */
    public void handleMsg(Object msgSender, Msg msg)  
    {
        if(msg.getType() == Msg.ERROR_TYPE)
        {
            ErrorMsg err = (ErrorMsg) msg;
            System.out.println( "Error occurred. Error code: " + err.getErrorCode() + ". Error message: " + err.getErrorString() );
        }
        else
        {
            printTrap((SnmpDataType)msg);
        }
    }
    
    /**
     * Prints trap to standard out
     */
    private void printTrap(SnmpDataType t)
    {
        System.out.println( "Received type:" + t.getType());
        if(t.getType() == SnmpConst.V1TRAP)
        {
            SnmpV1Trap trap = (SnmpV1Trap) t;
            String name = MibUtil.translateSnmpV1Trap(trap);
            System.out.println("Received SNMPv1 trap:");
            if(name != null) System.out.println( "Name:" + name);
            System.out.println("Community: " + trap.getCommunity());
            System.out.println("Enterprise OID: " + trap.getEnterprise());
            System.out.println("Ip Address: " + trap.getIpAddress());
            System.out.println("Generic: " + trap.getGenericString());
            System.out.println("Specific: " + trap.getSpecific());
            System.out.println("TimeStamp: " + trap.getTimestampString());

            printVarBinds(trap.getVarBinds());
        }
        else if(t.getType() == SnmpConst.V2TRAP)
        {
            SnmpV2Notification trap = (SnmpV2Notification)t;
            System.out.println( "Received SNMPv2/v3 trap from " + trap.getIpAddress());
            printV2Notification(trap);
        }
        else if(t.getType() == SnmpConst.INFORM)
        {
            SnmpV2Notification trap = (SnmpV2Notification)t;
            System.out.println( "Received SNMPv2/v3 inform from " + trap.getIpAddress());
            printV2Notification(trap);
        }
    }

    /**
     * Prints SNMPv2 trap or inform
     */
    private void printV2Notification(SnmpV2Notification trap)
    {
        int version = trap.getVersion();
        String ver = "" + (version == 1 ? (++version) : version);//change v2 version's from 1 to 2
        System.out.println("Version: " + ver);
        System.out.println( "Community: " + trap.getCommunity() );
        System.out.println( "sysUpTime.0: " + trap.getSysUpTimeString() );
        String name = MibUtil.translateOID("" + trap.getSnmpTrapOID(), false);
        if(name == null)
        {
            name = "" + trap.getSnmpTrapOID();
        }
        System.out.println("snmpTrapOID.0: " + name);
        printVarBinds(trap.getObjects());
    }
    
    private void printVarBinds(SnmpVarBind[] vbs)
    {
        for (int i = 0; i < vbs.length ; i++ )
        {
            SnmpVarBind vb = vbs[i];

            NameValue nv = MibUtil.translate(vb.getName(), vb.getValue().toString(), false);
            if(nv != null)
            {
                System.out.println( nv.getName() + ": " + nv.getValue());
            }
            else
            {
                System.out.println( vb.getName() + ": " + vb.getValue());
            }
        }
        System.out.println();
    }

    protected void printExample(String programName)
    {
        System.out.println( "java " + programName  );
        System.out.println( "java " + programName + 
                " -v 3  -u newUser -A abc12345 -X abc12345" );
    }

    protected void parseOptions(String[] args, 
                                String programName, 
                                boolean allowMultipleOIDs,
                                boolean allowNoOption)
    {
        super.parseOptions(args, programName, allowMultipleOIDs, allowNoOption);
        _port = Integer.parseInt(_parseArgs.getOptionValue('p', "162"));
    }

    /**
     * Creates a new instance of ParseArguments
     */
    protected ParseArguments newParseArgumentsInstance(String[] args)
    {
        return new ParseArguments(args, "?ho", "utvaAXxcpm");
    }
}// end of class 

