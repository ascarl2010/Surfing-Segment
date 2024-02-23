/**
 * IK 中文分词  版本 5.0
 * IK Analyzer release 5.0
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * 源代码由林良益(linliangyi2005@gmail.com)提供
 * 版权声明 2012，乌龙茶工作室
 * provided by Linliangyi and copyright 2012 by Oolong studio
 * <p>
 * 字符集识别工具类
 */
package com.talang.surfing.segment.util;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * 字符集识别工具类
 */
public class CharacterUtil {


    //无效的字符串
    public static final int CHAR_USELESS = -1;

    public static final int OTHERS = 0X00000000;

    public static final int CHAR_ARABIC = 0X00000001;

    public static final int CHAR_ENGLISH = 0X00000002;

    public static final int CHAR_SPACE = 0X00000003;

    public static final int CHAR_CONNECT = 0X00000009;

    public static final int PUNCTUATION = 0X000000011;

    private static Set<Character> SN_CHARS = Sets.newHashSet('φ', '￠', '℃', '～', 'Φ');

    private static Set<Character> CONNECT_CHARS = Sets.newHashSet('-', '*');


    //SN字符为：数字+字符+CONNECT_CHARS字符集
    public static boolean isSNCharacter(char input) {
        int charType = identifyCharType(input);
        return (charType == CHAR_ARABIC) || SN_CHARS.contains(input) || (charType == CHAR_ENGLISH);
    }

    public static boolean isDigital(char input) {
        int charType = identifyCharType(input);
        return charType == CHAR_ARABIC;
    }

    public static boolean isEnglish(char input) {
        int charType = identifyCharType(input);
        return charType == CHAR_ENGLISH;
    }

    //数字或者是英文
    public static boolean isDigitalEnglish(char input) {
        int charType = identifyCharType(input);
        return (charType == CHAR_ARABIC) || (charType == CHAR_ENGLISH);
    }

    //是否是连接字符串
    public static boolean isConnectCharacter(char input) {
        return CONNECT_CHARS.contains(input);
    }

    public static boolean isSpaceCharacter(char input) {
        return ' ' == input;
    }


    /**
     * 识别字符类型
     *
     * @param input
     * @return int CharacterUtil定义的字符类型常量
     */
    public static int identifyCharType(char input) {
        if ((input >= '0' && input <= '9') || input == '.') {
            return CHAR_ARABIC;
        } else if (CONNECT_CHARS.contains(input)) {
            return CHAR_CONNECT;
        } else if ((input >= 'a' && input <= 'z') || (input >= 'A' && input <= 'Z')) {
            return CHAR_ENGLISH;
        } else if ((input == ',')) {
            return PUNCTUATION;
        } else if (input == ' ') {
            return CHAR_SPACE;
        } else {
            //其他的不做处理的字符
            return OTHERS;
        }
    }

    /**
     * 进行字符规格化（全角转半角，大写转小写处理）
     *
     * @param input
     * @return char
     */
    public static char regularize(char input) {
        if (input == 12288) {
            input = (char) 32;

        } else if (input > 65280 && input < 65375) {
            input = (char) (input - 65248);

        } else if (input >= 'A' && input <= 'Z') {
            input += 32;
        }

        return input;
    }

    public static final boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }
}
