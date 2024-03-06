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
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*; 
import java.awt.event.*; 
import javax.swing.event.*;
import com.ireasoning.protocol.snmp.*;
import com.ireasoning.util.*;
import java.io.*;
import java.util.*;

/**
 * This class demonstrates how to use MIB (SMIv1/v2) parser to parse MIB and display MIB tree in a GUI program.
 * 
 */
public class mibparser 
{
    public static void main(String[] args)
    {
        if(args.length == 0)
        {
            System.out.println( "Usage: \njava mibparser mibFileName\njava mibparser mibFileNamesSeparatedByComma\njava mibparser directory");
            return;
        }
        String [] fileNames = null;
        String fileName = args[0];
        File file = new File(fileName);
        if(file.isDirectory())
        {//Load whole directory
            File [] files = file.listFiles();
            ArrayList fileList = new ArrayList();
            for (int i = 0; i < files.length ; i++) 
            {
                if(files[i].isDirectory()) continue;
                fileList.add(files[i].getAbsolutePath());
            }
            fileNames = new String[fileList.size()];
            for (int i = 0; i < fileNames.length ; i++) 
            {
                fileNames[i] = (String) fileList.get(i);
            }
        }
        else
        {
            fileNames = parseFileNames(fileName);
        }

        MibTreeNode node = null;
        try
        {
            //lenient parsing
            node = MibUtil.parseMibs(fileNames, false);
        }
        catch(MibParseException me)
        {
            System.out.println( me);
            me.printStackTrace();
            System.out.println( "error module name:" + me.getMibModuleName() );
            return;
        }
        catch(Exception e)
        {
            System.out.println( "Error occurs when loading MIB:" + fileName);
            System.out.println( e);
            e.printStackTrace();
            return;
        }
        //start to build DefaultMutableTreeNode, the simplest way is to use
        //DefaultMutableTreeNode dn = node.toJTreeNode();
        //But it does not have many info and not flexible
 
        // print out trap nodes
        // java.util.Vector v = node.getTrapNodes();
        // int trapNodeCount = v.size();
        // for (int i = 0; i < trapNodeCount; i++)
        // {
        //     TrapNode tnode = (TrapNode) v.get(i);
        //     System.out.println( tnode );
        // }
        
        DefaultMutableTreeNode dn = convertToJTreeNode(node);
        JTree jtree = new JTree(dn)
        {
            public String getToolTipText(MouseEvent evt) 
            {
                if (getRowForLocation(evt.getX(), evt.getY()) == -1) return null;    
                TreePath curPath = getPathForLocation(evt.getX(), evt.getY());
                return ((ToolTipTreeNode)curPath.getLastPathComponent()).getToolTipText();
            }
        };
        jtree.setToolTipText("");
        final JTextArea nodeDetails = new JTextArea();
        nodeDetails.setEditable(false);
        
        TreeSelectionListener listener = new TreeSelectionListener() 
        {
            public void valueChanged(TreeSelectionEvent e) 
            {
                MibTreeNode n = ((ToolTipTreeNode) e.getPath().getLastPathComponent()).getMibTreeNode();
                nodeDetails.setText(n.toString());
                nodeDetails.setCaretPosition(0);
            }
        };
        jtree.addTreeSelectionListener(listener);
        
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(jtree), new JScrollPane(nodeDetails));
        split.setDividerSize(3) ;
        split.setDividerLocation(400);
        JFrame f = new JFrame();
        f.getContentPane().add(split, BorderLayout.CENTER);
        f.setSize(800, 600);
        f.setVisible(true);
        f.addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent e)
            {
                System.exit(0);
            }
        });


        System.out.println( "********** Print out loaded MIB modules **********\r\n\r\n");
        Set info = MibUtil.getModulesInfo();
        Iterator it = info.iterator();
        while(it.hasNext())
        {
            MibModuleData m = (MibModuleData) it.next();
            System.out.println( "\r\n===========================================");
            System.out.println( m);
            Map imports = m.getImportsInfo();
            Set iset= imports.entrySet();
            Iterator eit = iset.iterator();
            while(eit.hasNext())
            {
                Map.Entry entry = (Map.Entry) eit.next();
                String imodule = (String) entry.getKey();
                HashSet nodes = (HashSet) entry.getValue();
                Iterator nodeIt = nodes.iterator();
                StringBuffer nodeBuf = new StringBuffer();
                boolean isFirst = true;
                while(nodeIt.hasNext())
                {
                    if(!isFirst) 
                    {
                        nodeBuf.append(", ");
                    }
                    nodeBuf.append(nodeIt.next());
                    isFirst =false;
                }
                System.out.println( "Import " + nodeBuf + " from " + imodule);
            }
        }
    }

    /**
     * Converts a MibTreeNode to DefaultMutableTreeNode in order to be able to used by JTree
     */
    public static ToolTipTreeNode convertToJTreeNode(MibTreeNode node)
    {
        return buildTree(node, null);
    }
    
    /**
     * Builds the tree recursively
     */
    private static ToolTipTreeNode buildTree(MibTreeNode mibNode, ToolTipTreeNode node)
    {
        if (mibNode == null)
        {
            return null;
        }
        if(node == null)
        {
            node = new ToolTipTreeNode(mibNode.getName().toString(), mibNode, getNodeTip(mibNode));
        }

        MibTreeNode n = (MibTreeNode) mibNode.getFirstChild();
        while(n != null)
        {
            ToolTipTreeNode child = new ToolTipTreeNode(n.getName().toString(), n, getNodeTip(n)); 
            node.add(child);
            buildTree(n, child);
            n = (MibTreeNode) n.getNextSibling();
        }              
        return node;
    } 

    private static String getNodeTip(MibTreeNode node)
    {
        String tip = node.getOID().toString();
        String syntax = node.getSyntaxType();
        if(syntax != null)
        {
            tip += ", type:" + syntax;
        }
        else
        {
            if(node.isSnmpV2TrapNode())
            {
                tip += ", SNMPv2 Trap Node";
            }
        }
        return tip;
    }
    
    static class ToolTipTreeNode extends DefaultMutableTreeNode 
    {
        private String toolTipText;
        private MibTreeNode mibTreeNode;

        public ToolTipTreeNode(String str, MibTreeNode node, String toolTipText) 
        {
            super(str);
            this.mibTreeNode = node;
            this.toolTipText = toolTipText;
        }

        public String getToolTipText() 
        {
            return toolTipText;
        }

        public MibTreeNode getMibTreeNode()
        {
            return mibTreeNode;
        }
    } 

    /**
     * Tokonized passed fileNames
     * @param fileNames file names separated by comma or semicolon
     */
    static String[] parseFileNames(String fileNames)
    {
        java.util.StringTokenizer st = new java.util.StringTokenizer(fileNames, ",;");
        String [] ret = new String[ st.countTokens() ];
        int i = 0;
        while (st.hasMoreTokens()) 
        {
            ret[i++] = st.nextToken();
        }
 
        return ret;
    }
}// end of class mibparser


