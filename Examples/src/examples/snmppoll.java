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
import com.ireasoning.util.ParseArguments;

/**
 * This class demonstrates polling agent (using SNMP GET_NEXT request) periodically.
 * <pre>
 * Example: 
 * java snmppoll -i 5 localhost .1.3.6.1.2.1.1.1.0 .1.3.6.1.2.1.1.3
 * </pre>
 */
public class snmppoll extends snmp
    implements Listener
{
    static int _interval = 3;//3 seconds;
    SnmpPoller _poller;
    
    public static void main(String[] args)  
    {
        snmppoll s = new snmppoll();
        s.parseOptions(args, "snmppoll");
        // s.printOptions();

        s.doIt();
    }

    public void handleMsg(Object sender, Msg msg)
    {
        if(msg.getType() != Msg.ERROR_TYPE)
        {
            SnmpPdu pdu = (SnmpPdu) msg;
            print(pdu);
        }
        else
        {
            ErrorMsg error = (ErrorMsg)msg;
            Exception e = error.getException();
            System.out.println( e );
        }
    }

    void doIt()
    {
        try
        {
            SnmpSession session = new SnmpSession(_host, _port, _community,
                    _community, _version);
            if(_isSnmpV3)
            {
                session.setV3Params(_user, _authProtocol, _authPassword, _privProtocol, _privPassword, _context, null);
            }

            _poller = new SnmpPoller(session);
            _poller.addListener(this);
            _poller.snmpGetNextPoll(_oids, _interval);
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
        System.out.println( "-i <n>\tpolling interval, in seconds");
    }
    
    protected void printExample(String programName)
    {
        System.out.println( "java " + programName + " -i 5 localhost .1.3.6.1.2.1.1.1.0 .1.3.6.1.2.1.1.3");
    }

    protected void parseOptions(String[] args, String program)
    {
        super.parseOptions(args, program);
        String in = _parseArgs.getOptionValue('i');
        if( in == null)
        {
            throw new RuntimeException("Interval must be specified.");
        }
        _interval = Integer.parseInt(in);
    }

    protected void printMoreOptions()
    {
        System.out.println( "interval =\t\t" + _interval);
    }

    /**
     * Creates a new instance of ParseArguments
     */
    protected ParseArguments newParseArgumentsInstance(String[] args)
    {
        return new ParseArguments(args, "?ho", "utvaAXxcpmi");
    }
    
}//end of class snmppoll


