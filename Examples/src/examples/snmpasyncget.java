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


import com.ireasoning.protocol.*;
import com.ireasoning.protocol.snmp.*;

/**
 * This class demonstrates asynchronously issuing SNMP GET request. For
 * asynchronous classes, they need to implements Listener interface, so they
 * can be notified if SnmpSession receives new messages.
 * <pre>
 * Example: 
 * java snmpasyncget localhost .1.3.6.1.2.1.1.1.0
 * </pre>
 */
public class snmpasyncget extends snmp implements Listener
{
    public static void main(String[] args)
    {
        snmpasyncget s = new snmpasyncget();
        s.parseOptions(args, "snmpasyncget");
        // s.printOptions();

        s.get();
    }
    
    private void get()
    {
        try
        {
            SnmpSession session = new SnmpSession(_host, _port, _community,
                    _community, _version);
            session.setRetries(0);
            session.setTimeoutForAsyncRequests(5000);//5 seconds timeout
            if(_isSnmpV3)
            {
                session.setV3Params(_user, _authProtocol, _authPassword, _privProtocol, _privPassword, _context, null);
            }
            session.addListener(this);
            SnmpPdu pdu = session.asyncSnmpGetRequest(_oids);//send out pdu, session will call handleMsg upon receiving response
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    /**
     * Another get method, uses the asyncSend method. Not used in this example
     */
    private void get2()
    {
        try
        {
            SnmpSession session = new SnmpSession(_host, _port, _community,
                    _community, _version);
            session.setTimeoutForAsyncRequests(5000);//5 seconds timeout
            if(_isSnmpV3)
            {
                session.setV3Params(_user, _authProtocol, _authPassword, _privProtocol, _privPassword, _context, null);
            }
            session.addListener(this);
            SnmpVarBind[]  vblist = new SnmpVarBind[_oids.length];
            for (int i = 0; i < vblist.length ; i++)
            {
                vblist[i] = new SnmpVarBind(_oids[i]);
            }
            SnmpPdu pdu = new SnmpPdu(SnmpConst.GET, vblist);
            session.asyncSend(pdu);//send out pdu, session will call handleMsg upon receiving response
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    /**
     * Callback method, as declared in Listener interface. It is invoked when responses are received.
     */
    public void handleMsg(Object session, Msg msg)
    { // received a pdu
        if(msg.getType() == Msg.ERROR_TYPE)
        {
            ErrorMsg err = (ErrorMsg) msg;
            if(err.getErrorCode () == ErrorMsg.TIMEOUT_ERROR)
            {
                TimeoutErrorMsg timeoutError = (TimeoutErrorMsg) err;
                System.out.println( "Time out. Request ID is " + timeoutError.getRequestID());
            }
            else
            {
                System.out.println( "Got an error:" );
                System.out.println( err.getException() );
            }
        }
        else
        {
            SnmpPdu pdu = (SnmpPdu) msg;
            print(pdu);
        }

        ((SnmpSession)session).close();// only expect one message in this example.close the session
    }

    protected void printExample(String programName)
    {
        System.out.println( "java " + programName + " localhost .1.3.6.1.2.1.1.1.0");
    }
}// end of class snmpasyncget 
