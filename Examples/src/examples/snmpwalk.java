/*
 * Copyright (c) 2002 iReasoning Networks. All Rights Reserved.
 * 
 * This SOURCE CODE FILE, which has been provided by iReasoning Networks as part
 * of an iReasoning Software product for use ONLY by licensed users of the product,
 * includes CONFIDENTIAL and PROPRIETARY information of iReasoning Networks.  
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


import com.ireasoning.protocol.Listener;
import com.ireasoning.protocol.Msg;
import com.ireasoning.protocol.TimeoutException;
import com.ireasoning.protocol.snmp.*;
import com.ireasoning.util.ParseArguments;
import java.io.IOException;

/**
 * This class demonstrates three different implementations of snmp walk. 
 * <br>
 * walk1() demonstrates use of SnmpSession.snmpWalk to implement snmp (simplest
 * way) <br>
 * walk2() demonstrates use of SnmpSession.snmpGetNextRequest to implement snmp
 * walk<br>
 * walk3() demonstrates use of primitive SnmpSession.send to implement
 * snmp walk<br>
 * <pre>
 * Example:
 * java snmpwalk localhost .1.3
 * java snmpwalk localhost
 * java snmpwalk localhost -v 3 -u newUser -A abc12345 -X abc12345 .1.3
 *  
 * </pre>
 */
public class snmpwalk extends snmp implements Listener
{
    static int _walkMethod = 1;
    
    public static void main(String[] args)
    {
        snmpwalk s = new snmpwalk();
        s.parseOptions(args, "snmpwalk", false, false);
        // s.printOptions();

        // starting to walk...
       
        if(_walkMethod == 1)
        {// use SnmpSession.snmpWalk
            s.walk1();
        }
        else if(_walkMethod == 2)
        {// use SnmpSession.snmpGetNextRequest
            s.walk2();
        }
        else if(_walkMethod == 3)
        {// use SnmpSession.send
            s.walk3();
        }
    }
    
    /**
     * This method demonstrates use of SnmpSession.snmpWalk to implement snmp
     * walk. It's the simplest way to do snmp walk.
     * @see #handleMsg
     */
    private void walk1()
    {
        try
        {
            SnmpSession session = new SnmpSession(_host, _port, _community,
                    _community, _version, _transportLayer);
            if(_isSnmpV3)
            {
                session.setV3Params(_user, _authProtocol, _authPassword,
                               _privProtocol, _privPassword, _context, null);
            }
            session.snmpWalk(_oids[0], this);
        }
        catch(TimeoutException timeout)
        {
            System.out.println( "time out");
        }
        catch(IOException ie)
        {
            System.out.println(ie);
            ie.printStackTrace();
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    /**
     * Callback method. walk1() will indirectly invoke this method.
     */
    public void handleMsg(Object session, Msg msg)
    {
        SnmpPdu pdu = (SnmpPdu) msg;
        if(pdu.getErrorStatus() != 0)
        {
            System.out.println( "Error: " + SnmpErrorStatus.ERR_STRINGS[pdu.getErrorStatus()] );
            return;
        }
        if(pdu.isSnmpV3AuthenticationFailed())
        {
            System.out.println( "Authentication failed");
            return;
        }
        if(!pdu.hasMore())
        {
            System.out.println( "<End of MIB View Reached>");
            ((SnmpSession)session).close();
            return;
        }

        print(pdu);
    }
    
    /**
     * This method demonstrates use of SnmpSession.snmpGetNextRequest to implement snmp
     * walk
     */
    private void walk2()
    {
        try
        {
            SnmpTarget target = new SnmpTarget(_host, _port, _community, _community, _version);
            SnmpSession session = new SnmpSession(target, _transportLayer);
            if(_isSnmpV3)
            {
                session.setV3Params(_user, _authProtocol, _authPassword,
                                    _privProtocol, _privPassword, _context, null);
            }
            
            SnmpPdu retPdu = session.snmpGetNextRequest(_oids[0]);
            if(!retPdu.hasMore())
            {
                System.out.println("End of mib view reached");
                return;
            }

            print(retPdu);
            
            while (true)
            {
                retPdu = session.snmpGetNextRequest(retPdu.getFirstVarBind().getName());
                if(!retPdu.hasMore())
                {
                    System.out.println("End of mib view reached");
                    break;
                }
                print(retPdu);
            }
            session.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    /**
     * This method demonstrates use of primitive SnmpSession.send to implement
     * snmp walk
     */
    private void walk3()
    {
        try
        {
            SnmpTarget target = new SnmpTarget(_host, _port, _community, _community);
            SnmpSession session = new SnmpSession(target, _transportLayer);
            if(_isSnmpV3)
            {
                session.setV3Params(_user, _authProtocol, _authPassword, _privProtocol, _privPassword, _context, null);
            }
            
            SnmpVarBind[]  vblist = { new SnmpVarBind(_oids[0]) };
            SnmpPdu pdu = new SnmpPdu(SnmpConst.GET_NEXT, vblist);
            SnmpPdu retPdu = null;
            retPdu = session.send(pdu);
            if(!retPdu.hasMore())
            {
                System.out.println("End of mib view reached");
                return;
            }

            print(retPdu);

            while (true)
            {
                pdu.setOID(retPdu.getFirstVarBind().getName());
                retPdu = session.send(pdu);
                if(!retPdu.hasMore())
                {
                    System.out.println("End of mib view reached");
                    break;
                }
                print(retPdu);
            }
            session.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------------------------
    // Parsing command line options
    // ----------------------------------------------------------------------
    
    protected void moreOptions()
    {
        System.out.println( "-w {1|2|3} which walk method to use, default is 1");
    }
    
    protected void parseOptions(String[] args, 
                                String programName, 
                                boolean allowMultipleOIDs,
                                boolean allowNoOption)
    {
        super.parseOptions(args, programName, allowMultipleOIDs, allowNoOption);
        _walkMethod = Integer.parseInt(_parseArgs.getOptionValue('w', "1"));
    }

    protected void printMoreOptions()
    {
        System.out.println( "walk-method =\t\t" + _walkMethod);
    }

    /**
     * Creates a new instance of ParseArguments
     */
    protected ParseArguments newParseArgumentsInstance(String[] args)
    {
        return new ParseArguments(args, "?ho", "utvaAXxcpmnrwk");
    }
    
}// end of class snmpwalk 
