public class TokenXmlBuilder {
    private StringBuilder stringBuilder = new StringBuilder();
    private int tab = 0;
    public TokenXmlBuilder(){
    }
    public void addNodeAndAttribute(String node ,String attribute){
        stringBuilder.append(String.format("%s<%s> ",tabNumber(),node));
        if ("<".equals(attribute)){
            attribute = "&lt;";
        }
        if (">".equals(attribute)){
            attribute = "&gt;";
        }
        if ("&".equals(attribute)){
            attribute = "amp;";
        }
        stringBuilder.append(attribute);
        stringBuilder.append(String.format(" </%s>\n",node));
    }

    private String startNode(String value){
        return String.format("%s<%s>\n",tabNumber(),value);
    }

    private String endNode(String value){
        return String.format("%s</%s>\n",tabNumber(),value);
    }

    public void setStartNode(String node){
       stringBuilder.append(startNode(node));
       tab++;
    }

    public void setEndNode(String node){
        tab--;
        stringBuilder.append(endNode(node));
    }

    private String tabNumber(){
        StringBuilder tabs= new StringBuilder();
        for (int i=0;i<tab;i++){
            tabs.append("  ");
        }
        return tabs.toString();
    }
    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
