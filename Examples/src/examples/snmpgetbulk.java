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


import com.ireasoning.protocol.snmp.*;
import com.ireasoning.util.ParseArguments;

/**
 * This class demonstrates an implementation of SNMP WALK. 
 * Note: Agent has to be able to support SNMPV2.
 * <pre>
 * Example: 
 * java snmpgetbulk -n 1 -r 5 localhost .1.3.6.1.2.1.1.1.0 .1.3.6.1.2.1.1.3
 * </pre>
 */
public class snmpgetbulk extends snmp
{
    int _nonRepeators;
    int _maxRepetitions;
    
    public static void main(String[] args)
    {
        snmpgetbulk s = new snmpgetbulk();
        s.parseOptions(args, "snmpgetbulk");
        // s.printOptions();

        s.getbulk();
    }
    
    private void getbulk()
    {
        try
        {
            SnmpSession session = new SnmpSession(_host, _port, _community,
                    _community, _version, _transportLayer);
            if(_isSnmpV3)
            {
                session.setV3Params(_user, _authProtocol, _authPassword, _privProtocol, _privPassword, _context, null);
            }
            
            SnmpPdu retPdu = session.snmpGetBulkRequest(_oids, _nonRepeators, _maxRepetitions);
            
            print(retPdu);
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
        System.out.println( "-n <n>\tNon-Repeators value");
        System.out.println( "-r <r>\tMax-Repetitions value");
    }
    
    protected void printExample(String programName)
    {
        System.out.println( "java " + programName + " -n 1 -r 5 localhost .1.3.6.1.2.1.1.1.0 .1.3.6.1.2.1.1.3");
    }

    protected void parseOptions(String[] args, String program)
    {
        super.parseOptions(args, program);
        String nr = _parseArgs.getOptionValue('n');
        if(nr == null)
        {
            throw new RuntimeException("Non-Repeators must be specified.");
        }
        _nonRepeators = Integer.parseInt(nr);

        String mr = _parseArgs.getOptionValue('r');
        if(mr == null)
        {
            throw new RuntimeException("Max-Repetitions must be specified.");
        }
        _maxRepetitions = Integer.parseInt(mr);
    }

    protected void printMoreOptions()
    {
        System.out.println( "Non-Repeators =\t\t" + _nonRepeators);
        System.out.println( "Max-Repetitions =\t" + _maxRepetitions);
    }

    /**
     * Creates a new instance of ParseArguments
     */
    protected ParseArguments newParseArgumentsInstance(String[] args)
    {
        return new ParseArguments(args, "?ho", "utvaAXxcpmnrk");
    }
}// end of class snmpgetbulk 
