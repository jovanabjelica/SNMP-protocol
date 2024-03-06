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
 * This class demonstrates an implementation of SNMP GET_SUBTREE operation by
 * using more efficient GET_BULK operation.
 * Note: Agent has to be able to support SNMPV2.
 * <pre>
 * Example: 
 * java snmpbulkgetsubtree localhost .1.3.6.1.2.1.2.2.1 
 * java snmpbulkgetsubtree localhost iftable 
 * java snmpbulkgetsubtree localhost ifTable 
 * java snmpbulkgetsubtree localhost system 
 * java snmpbulkgetsubtree -o localhost ipRouteTable 
 * java snmpbulkgetsubtree localhost mib-2
 * </pre>
 */
public class snmpbulkgetsubtree extends snmp 
    implements Listener
{
    
    public static void main(String[] args)
    {
        snmpbulkgetsubtree s = new snmpbulkgetsubtree();
        s.parseOptions(args, "snmpbulkgetsubtree", false, false);
        s.getTable();
    }
    
    private void getTable()
    {
        try
        {
            SnmpSession session = new SnmpSession(_host, _port, _community,
                    _community, _version, _transportLayer);
            if(_isSnmpV3)
            {
                session.setV3Params(_user, _authProtocol, _authPassword, _privProtocol, _privPassword, _context, null);
            }
            session.snmpBulkGetSubtree(_oids[0], this);
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    /**
     * Callback method, got notified when a new snmp message is received
     */
    public void handleMsg(Object session, Msg msg)
    {
        SnmpPdu pdu = (SnmpPdu) msg;
        if(!pdu.hasMore()) 
        {
            System.out.println( "<End of Subtree Reached>");
            ((SnmpSession)session).close();
            return;
        }

        print(pdu);

    }
 
    protected void printExample(String programName)
    {
        System.out.println( "java " + programName + " localhost .1.3.6.1.2.1.2.2.1");
        System.out.println( "java " + programName + " localhost iftable");
        System.out.println( "java " + programName + " localhost ifTable");
        System.out.println( "java " + programName + " localhost system");
        System.out.println( "java " + programName + " -o localhost ipRouteTable");
        System.out.println( "java " + programName + " localhost mib-2");
    }

}// end of class snmpbulkgetsubtree 
