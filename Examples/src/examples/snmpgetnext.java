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


import com.ireasoning.protocol.TimeoutException;
import com.ireasoning.protocol.snmp.*;
import com.ireasoning.util.ParseArguments;
import java.io.IOException;

/**
 * This class demonstrates two different implementations of snmp get_next operation. 
 * <br>
 * 1. Use SnmpSession.snmpGetNextRequest. This is a more convinient way. <br>
 * 2. Use primitive SnmpSession.send <br>
 * <pre>
 * Example:
 * java snmpgetnext localhost .1.3.6.1.2.1.1.3.0
 * java snmpgetnext localhost sysUpTime
 * java snmpgetnext localhost -v 3 -u newUser -A abc12345 -X abc12345 .1.3.2
 * 
 * </pre>
 */
public class snmpgetnext extends snmp
{
    /**
     * Which getnext method to use
     */
    static int _getNextMethod = 1;

    public static void main(String[] args)
    {
        snmpgetnext s = new snmpgetnext();
        s.parseOptions(args, "snmpgetnext");
        // s.printOptions();

       
        if(_getNextMethod == 1)
        {// use SnmpSession.snmpGetNextRequest
            s.getnext1();
        }
        else if(_getNextMethod == 2)
        {// use SnmpSession.send
            s.getnext2();
        }
    }
    
    /**
     * use SnmpSession.snmpGetNextRequest
     */
    private void getnext1()
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
            
            SnmpPdu retPdu = session.snmpGetNextRequest(_oids);

            print(retPdu);
            session.close();
        }
        catch(TimeoutException timeout)
        {
            System.out.println( "time out");
        }
        catch(IOException e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    /**
     * use primitive SnmpSession.send
     */
    private void getnext2()
    {
        try
        {
            SnmpTarget target = new SnmpTarget(_host, _port,
                               _community, _community, _version);
            SnmpSession session = new SnmpSession(target, _transportLayer);
            if(_isSnmpV3)
            {
                session.setV3Params(_user, _authProtocol, _authPassword,
                                    _privProtocol, _privPassword, _context, null);
            }
            
            SnmpVarBind[]  vblist = new SnmpVarBind[_oids.length];
            for (int i = 0; i < vblist.length ; i++) 
            {
                vblist[i] = new SnmpVarBind(_oids[i]);
            }
            SnmpPdu pdu = new SnmpPdu(SnmpConst.GET_NEXT, vblist);
            SnmpPdu retPdu = session.send(pdu);

            print(retPdu);
            session.close();
        }
        catch(TimeoutException timeout)
        {
            System.out.println( "time out");
        }
        catch(IOException e)
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
        System.out.println( "-w {1|2} which getnext method to use, default is 1");
    }
    
    protected void parseOptions(String[] args, String program)
    {
        super.parseOptions(args, program);
        _getNextMethod = Integer.parseInt(_parseArgs.getOptionValue('w', "1"));
    }

    protected void printMoreOptions()
    {
        System.out.println( "getnext-method =\t" + _getNextMethod);
    }

    /**
     * Prints out the example usage
     */
    protected void printExample(String programName)
    {
        System.out.println( "java " + programName + " localhost .1.3.6.1.2.1.1.3.0");
        System.out.println( "java " + programName + " localhost sysUpTime");
        System.out.println( "java " + programName + 
            " localhost -v 3 -u newUser -A abc12345 -X abc12345 .1.3" );
    }
    
    /**
     * Creates a new instance of ParseArguments
     */
    protected ParseArguments newParseArgumentsInstance(String[] args)
    {
        return new ParseArguments(args, "?ho", "utvaAXxcpmnrwk");
    }
    
}// end of class snmpgetnext 
