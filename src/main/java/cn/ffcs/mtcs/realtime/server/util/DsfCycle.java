package cn.ffcs.mtcs.realtime.server.util;

import java.util.ArrayList;
import java.util.List;

//寻找有向图 闭环类
public class DsfCycle {


    public  DsfCycle()  {
        this.nodes= new ArrayList<String>();
        this.adjacencyMatrix= new  int[MAX_NODE_COUNT][MAX_NODE_COUNT];
    }
    
    /**
     * 限制node最大数
     */
    public  static int MAX_NODE_COUNT = 1000;

    /**
     * node集合
     */
    public  static List<String> nodes= null;

    /**
     * 有向图的邻接矩阵
     */
    public  static int[][] adjacencyMatrix = null;

    /**
     * @Title addNode
     * @Description 添加节点
     * @param nodeName
     * @return
     */
    public  int addNode(String nodeName){
       
        if(!nodes.contains(nodeName)) {
            if(nodes.size()>=MAX_NODE_COUNT) {
                System.out.println("nodes超长:"+nodeName);
                return -1;
            }
            nodes.add(nodeName);
            return  nodes.size()-1;
        }
        return nodes.indexOf(nodeName);
    }

    /**
     * @Title addLine
     * @Description 添加线，初始化邻接矩阵
     * @param startNode
     * @param endNode
     */
    public  void addLine(String startNode, String endNode){
        int startIndex = addNode(startNode);
        int endIndex = addNode(endNode);
        if(startIndex>=0&&endIndex>=0) {
            adjacencyMatrix[startIndex][endIndex] = 1 ;
        }
    }

    /**
     * @Title find
     * @Description 寻找闭环
     * @return
     */
    public static List<String>  find() {
        // 从出发节点到当前节点的轨迹
        List<Integer> trace =new ArrayList<Integer>();
        //返回值
        List<String> reslut = new ArrayList<>();
        if(adjacencyMatrix.length>0) {
            findCycle(0, trace,reslut);
        }
        if(reslut.size()==0) {
            reslut.add("no cycle!");
        }
        return reslut;
    }
    /**
     * @Title findCycle
     * @param v
     * @param trace
     * @param reslut
     */
    public static void findCycle(int v,List<Integer> trace,List<String> reslut)
    {
        int j;
        //添加闭环信息
        if((j=trace.indexOf(v))!=-1) {
            StringBuffer sb = new StringBuffer();
            String startNode = nodes.get(trace.get(j));
            while(j<trace.size()) {
                sb.append(nodes.get(trace.get(j))+"-");
                j++;
            }
            reslut.add("cycle:"+sb.toString()+startNode);
            return;
        }
        trace.add(v);
        for(int i=0;i<nodes.size();i++){
            if(adjacencyMatrix[v][i]==1) {
                findCycle(i,trace,reslut);
            }
        }
        trace.remove(trace.size()-1);
    }

    //测试
    public static void main(String[] args) {
        DsfCycle dsfCycle=new DsfCycle();

        dsfCycle.addLine("1", "2");
        dsfCycle.addLine("2", "3");
        dsfCycle.addLine("3", "4");
       // dsfCycle.addLine("4", "1");
        dsfCycle.addLine("4", "5");
        dsfCycle.addLine("5", "6");
        List<String> reslut = dsfCycle.find();
        for (String string : reslut) {
            System.out.println(string);
        }
    }
}