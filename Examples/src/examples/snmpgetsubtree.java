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
 * This class demonstrates two different implementations of SNMP GET_SUBTREE operation. 
 * <br>
 * 1. Use "void SnmpSession.snmpGetSubtree(String startingOID, Listener callback)"
 * <br>
 * 2. Use synchronous "SnmpVarBind [] SnmpSession.snmpGetSubtree(String startingOID)"
 * <pre>
 * Example:
 * java snmpgetsubtree localhost .1.3.6.1.2.1.2.2.1
 * java snmpgetsubtree localhost iftable
 * java snmpgetsubtree localhost ifTable
 * java snmpgetsubtree localhost system
 * java snmpgetsubtree -o localhost ipRouteTable
 * java snmpgetsubtree localhost mib-2
 * 
 * </pre>
 */
public class snmpgetsubtree  extends snmp  implements Listener
{
    static int _method = 1;

    public static void main(String[] args)
    {
        snmpgetsubtree s = new snmpgetsubtree();
        s.parseOptions(args, "snmpgetsubtree");
        // s.printOptions();

       
        if(_method == 1)
        {// By default, use SnmpSession.snmpGetSubtree(startingOID, callback)
            s.getSubtree1();
        }
        else if(_method == 2)
        {// use synchronous SnmpSession.snmpGetSubtree
            s.getSubtree2();
        }
    }
    
    public void handleMsg(Object session, Msg msg)
    {
        SnmpPdu pdu = (SnmpPdu) msg;
        if(pdu.isSnmpV3AuthenticationFailed())
        {
            System.out.println( "AuthFailed");
            return;
        }
        if(!pdu.hasMore())
        {
            System.out.println( "<End of Subtree Reached>");
            ((SnmpSession)session).close();
            return;
        }
        print(pdu);
    }
    
    /**
     * Use callback mechanism, handleMsg will be called by SnmpSession
     */
    private void getSubtree1()
    {
        try
        {
            SnmpSession session = new SnmpSession(_host, _port, _community,
                    _community, _version, _transportLayer);
            if(_isSnmpV3)
            {
                session.setV3Params(_user, _authProtocol, _authPassword, _privProtocol, _privPassword, _context, null);
            }
            session.snmpGetSubtree(_oids[0], this);
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    /**
     * Use synchronous SnmpSession.snmpGetSubtree method
     */
    private void getSubtree2()
    {
        try
        {
            SnmpSession session = new SnmpSession(_host, _port, _community,
                    _community, _version, _transportLayer);
            if(_isSnmpV3)
            {
                session.setV3Params(_user, _authProtocol, _authPassword, _privProtocol, _privPassword, _context, null);
            }
            SnmpError error = new SnmpError();
            SnmpVarBind[]  varbinds = session.snmpGetSubtree(_oids[0], error);
            if(error.isAuthFailed())
            {
                System.out.println( "Authentication failed");
            }
            else if(error.getErrorStatus() != 0)
            {
                System.out.println(SnmpPdu.getErrorStatusString(error.getErrorStatus()));
            }
            else
            {
                print(varbinds);
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
    
    protected void printExample(String programName)
    {
        System.out.println( "java " + programName + " localhost .1.3.6.1.2.1.2.2.1");
        System.out.println( "java " + programName + " localhost iftable");
        System.out.println( "java " + programName + " localhost ifTable");
        System.out.println( "java " + programName + " localhost system");
        System.out.println( "java " + programName + " -o localhost ipRouteTable");
        System.out.println( "java " + programName + " localhost mib-2");
    }

    protected void moreOptions()
    {
        System.out.println( "-w {1|2} which get method to use, default is 1");
    }
    
    protected void parseOptions(String[] args, String program)
    {
        super.parseOptions(args, program);
        _method = Integer.parseInt(_parseArgs.getOptionValue('w', "1"));
    }

    protected void printMoreOptions()
    {
        System.out.println( "getSubtree =\t\t" + _method);
    }

    /**
     * Creates a new instance of ParseArguments
     */
    protected ParseArguments newParseArgumentsInstance(String[] args)
    {
        return new ParseArguments(args, "?ho", "utvaAXxcpmnrwk");
    }
    
}// end of class snmpgetsubtree 
