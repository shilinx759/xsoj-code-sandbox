package com.shilinx.xsojcodesandbox.attackexamle;

import java.util.ArrayList;
import java.util.List;

/**
 * 无限占用内存攻击
 * @author slx
 */
public class MemoryAttack {
    public static void main(String[] args) {
        List<byte[]> bytes = new ArrayList<>();
        while (true) {
            bytes.add(new byte[1000]);
        }
    }
}
