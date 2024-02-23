package com.talang.surfing.segment.core;

import com.google.common.collect.Lists;
import com.talang.surfing.segment.util.CharacterUtil;

import java.util.List;

public class SnSegmenter implements ISegmenter {

    private int groupId = 0;
    //sn分词起始位置
    private int index = -1;
    private int end = -1;

    //上一个字符是空格或连接字符
    private boolean lastTypeIsConnectOrSpace = false;

    private List<Integer> starts = Lists.newArrayList();

    private List<Integer> ends = Lists.newArrayList();

    private List<Integer> connectPos = Lists.newArrayList();

    private int lastPos = -1;

    //6002ZZ  找到前缀数字或字母，这个串中要分出6002
    private int lastCharType = CharacterUtil.OTHERS;

    @Override
    public void analyzer(Context context) {
        boolean isSnChar = CharacterUtil.isSNCharacter(context.getCurrentChar());
        boolean isConnectChar = CharacterUtil.isConnectCharacter(context.getCurrentChar());
        if (isConnectChar) {
            connectPos.add(context.getCursor());
        }
        boolean isSpace = CharacterUtil.isSpaceCharacter(context.getCurrentChar());
        int currentCharType = CharacterUtil.identifyCharType(context.getCurrentChar());

        //如果是sn字符串
        if (isSnChar) {
            if (index == -1) {
                index = context.getCursor();
                addStartPos(context.getCursor());
            }
            //空格后的首个sn字符，加入开始
            if (lastTypeIsConnectOrSpace) {
                addStartPos(context.getCursor());
            }
            //当前字符为英文或数字，上一字符为数字和英文的情况，增加起始字符串
            if (currentCharType == CharacterUtil.CHAR_ENGLISH && lastCharType == CharacterUtil.CHAR_ARABIC) {
                addStartPos(context.getCursor());
                addEndPos(context.getCursor() - 1);
            } else if (currentCharType == CharacterUtil.CHAR_ARABIC && lastCharType == CharacterUtil.CHAR_ENGLISH) {
                addStartPos(context.getCursor());
                addEndPos(context.getCursor() - 1);
            }
        }

        //如果是连接符，并且进行了sn分词，则加入结束
        if (/*isSpace || */ isConnectChar) {
            if (this.index != -1) {
                addEndPos(context.getCursor() - 1);
            }
        }

        lastCharType = currentCharType;
        //最后一个字符
        if (context.isLastChar()) {
            if (isSnChar) {
                lastPos = context.getCursor();
                addEndPos(context.getCursor());
            } else {
                lastPos = context.getCursor() -1;
                addEndPos(context.getCursor()-1);
            }
            doAddSnSegment(context);
        }

        //非sn字符串的首个字符，
        if (!isSnChar && !isConnectChar) {
            if (this.index != -1 && lastTypeIsConnectOrSpace != true) {
                addEndPos(context.getCursor() - 1);
            }
            lastPos = context.getCursor() - 1;
            doAddSnSegment(context);
        }

        if (isConnectChar || isSpace) {
            lastTypeIsConnectOrSpace = true;
        } else {
            lastTypeIsConnectOrSpace = false;
        }
    }

    private void addStartPos(int pos) {
        if(!starts.contains(pos)) {
            starts.add(pos);
        }
    }

    private void addEndPos(int pos){
        if(!ends.contains(pos)) {
            ends.add(pos);
        }
    }

    private void doAddSnSegment(Context context) {
        List<Lexeme> toAddLexems = Lists.newArrayList();
        for (int i = 0; i < starts.size(); i++) {
            for (int j = 0; j < ends.size(); j++) {
                int begin = starts.get(i);
                int finish = ends.get(j);
                if (finish >= begin && finish - begin >=2) {
                    Lexeme lexeme = new Lexeme(begin, finish, DictType.SN, context.getInput());
                    toAddLexems.add(lexeme);
                }
            }
        }

        //包含连接符
        if (connectPos.size() > 0) {
            for(Lexeme lexeme : toAddLexems) {
                int begin = lexeme.getBegin();
                int end = lexeme.getEnd();
                if(isSameSegment(begin, end)) {
                    context.addLexeme(lexeme);
                }
            }
        } else {
            if (toAddLexems.size() > 0) {
                for(Lexeme lexeme : toAddLexems) {
                    context.addLexeme(lexeme);
                }
            }
        }
        reset();
    }

    //是否是同一段，同一段则允许，不同段不允许，整串除外
    private boolean isSameSegment(int begin, int end) {
        if(begin == this.index && end == lastPos) {
            return true;
        }

        int  allowStart = 0;
        connectPos.add(lastPos);
        for (int i = 0; i < connectPos.size(); i++) {
            int allowEnd = connectPos.get(i);
            if(begin >= allowStart && end <= allowEnd) {
                return true;
            }
            allowStart = allowEnd;
        }
        return false;
    }

    //包含连字符的情况处理单段
    private void doAddSingleConnect(List<Lexeme> toAddLexems, Context context) {
        List<ConnectLexemePos> connectLexemePos = Lists.newArrayList();
        int start = index;
        for (int i = 0; i < connectPos.size(); i++) {
            end = connectPos.get(i) -1;
            connectLexemePos.add(new ConnectLexemePos(start, end));
            start = connectPos.get(i) +1;
        }
        connectLexemePos.add(new ConnectLexemePos(this.index, lastPos));
        connectLexemePos.add(new ConnectLexemePos(connectPos.get(connectPos.size() -1 ) + 1, lastPos));
        for (Lexeme toAddLexeme : toAddLexems) {
            int beginPos = toAddLexeme.getBegin();
            int endPos = toAddLexeme.getEnd();
            addSingleConnectSegments(context, beginPos, endPos, toAddLexeme, connectLexemePos);
        }
    }

    //分成单段
    private void addSingleConnectSegments(Context context, int beginPos, int endPos, Lexeme toAddLexeme, List<ConnectLexemePos> connectLexemePos) {
        for(ConnectLexemePos curPos : connectLexemePos) {
            if(beginPos == curPos.getStart() && endPos == curPos.getEnd()) {
                context.addLexeme(toAddLexeme);
            }
        }
    }

    //分成多段1，2，3分为(1, 1-2, 1-2-3, 2-3)
    private void doAddMultiConnect(Context context, int beginPos, int endPos, Lexeme toAddLexeme) {
        if (isValidStartPosForConnectors(beginPos) && isValidEndPosForConnectors(endPos)) {
            context.addResult(toAddLexeme);
        }
    }

    //是否是有效的起始位置
    private boolean isValidStartPosForConnectors(int pos) {
        if (pos == 0) {
            return true;
        }
        for (int conPos : connectPos) {
            if (pos == conPos + 1) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidEndPosForConnectors(int pos) {
        if (pos == lastPos) {
            return true;
        }
        for (int conPos : connectPos) {
            if (pos == conPos - 1) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void reset() {
        groupId++;
        this.index = -1;
        starts.clear();
        connectPos.clear();
        lastPos = -1;
        ends.clear();
    }

    @Override
    public String getName() {
        return "sn";
    }
}
