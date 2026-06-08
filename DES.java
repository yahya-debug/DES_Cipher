public class DES {
    // Permutation Tables
    private static final int[] IP = {
            58, 50, 42, 34, 26, 18, 10,  2,
            60, 52, 44, 36, 28, 20, 12,  4,
            62, 54, 46, 38, 30, 22, 14,  6,
            64, 56, 48, 40, 32, 24, 16,  8,
            57, 49, 41, 33, 25, 17,  9,  1,
            59, 51, 43, 35, 27, 19, 11,  3,
            61, 53, 45, 37, 29, 21, 13,  5,
            63, 55, 47, 39, 31, 23, 15,  7
    };

    private static final int[] FP = {
            40,  8, 48, 16, 56, 24, 64, 32,
            39,  7, 47, 15, 55, 23, 63, 31,
            38,  6, 46, 14, 54, 22, 62, 30,
            37,  5, 45, 13, 53, 21, 61, 29,
            36,  4, 44, 12, 52, 20, 60, 28,
            35,  3, 43, 11, 51, 19, 59, 27,
            34,  2, 42, 10, 50, 18, 58, 26,
            33,  1, 41,  9, 49, 17, 57, 25
    };

    // Expansion Permutation E: 32 > 48 bits
    private static final int[] E = {
            32,  1,  2,  3,  4,  5,
            4,  5,  6,  7,  8,  9,
            8,  9, 10, 11, 12, 13,
            12, 13, 14, 15, 16, 17,
            16, 17, 18, 19, 20, 21,
            20, 21, 22, 23, 24, 25,
            24, 25, 26, 27, 28, 29,
            28, 29, 30, 31, 32,  1
    };

    // Straight Permutation P: 32 > 32 bits
    private static final int[] P_TABLE = {
            16,  7, 20, 21, 29, 12, 28, 17,
            1, 15, 23, 26,  5, 18, 31, 10,
            2,  8, 24, 14, 32, 27,  3,  9,
            19, 13, 30,  6, 22, 11,  4, 25
    };

    // Permuted Choice 1 (PC-1): 64 > 56 bits (removes parity bits)
    private static final int[] PC1 = {
            57, 49, 41, 33, 25, 17,  9,  1,
            58, 50, 42, 34, 26, 18, 10,  2,
            59, 51, 43, 35, 27, 19, 11,  3,
            60, 52, 44, 36, 63, 55, 47, 39,
            31, 23, 15,  7, 62, 54, 46, 38,
            30, 22, 14,  6, 61, 53, 45, 37,
            29, 21, 13,  5, 28, 20, 12,  4
    };

    // Permuted Choice 2 (PC-2): 56 > 48 bits (round subkey selection)
    private static final int[] PC2 = {
            14, 17, 11, 24,  1,  5,  3, 28,
            15,  6, 21, 10, 23, 19, 12,  4,
            26,  8, 16,  7, 27, 20, 13,  2,
            41, 52, 31, 37, 47, 55, 30, 40,
            51, 45, 33, 48, 44, 49, 39, 56,
            34, 53, 46, 42, 50, 36, 29, 32
    };

    // Left-rotation amounts per round: 1-bit for rounds 1,2,9,16; 2-bits otherwise
    private static final int[] SHIFTS = { 1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1 };

    // S boxes: 8 tables, each 4 * 16

    private static final int[][][] SBOXES = {
            // S1
            {{ 14,  4, 13,  1,  2, 15, 11,  8,  3, 10,  6, 12,  5,  9,  0,  7 },
                    {  0, 15,  7,  4, 14,  2, 13, 10,  3,  6, 12, 11,  9,  5,  3,  8 },
                    {  4,  1, 14,  8, 13,  6,  2, 11, 15, 12,  9,  7,  3, 10,  5,  0 },
                    { 15, 12,  8,  2,  4,  9,  1,  7,  5, 11,  3, 14, 10,  0,  6, 13 }},
            // S2
            {{ 15,  1,  8, 14,  6, 11,  3,  4,  9,  7,  2, 13, 12,  0,  5, 10 },
                    {  3, 13,  4,  7, 15,  2,  8, 14, 12,  0,  1, 10,  6,  9, 11,  5 },
                    {  0, 14,  7, 11, 10,  4, 13,  1,  5,  8, 12,  6,  9,  3,  2, 15 },
                    { 13,  8, 10,  1,  3, 15,  4,  2, 11,  6,  7, 12,  0,  5, 14,  9 }},
            // S3
            {{ 10,  0,  9, 14,  6,  3, 15,  5,  1, 13, 12,  7, 11,  4,  2,  8 },
                    { 13,  7,  0,  9,  3,  4,  6, 10,  2,  8,  5, 14, 12, 11, 15,  1 },
                    { 13,  6,  4,  9,  8, 15,  3,  0, 11,  1,  2, 12,  5, 10, 14,  7 },
                    {  1, 10, 13,  0,  6,  9,  8,  7,  4, 15, 14,  3, 11,  5,  2, 12 }},
            // S4
            {{  7, 13, 14,  3,  0,  6,  9, 10,  1,  2,  8,  5, 11, 12,  4, 15 },
                    { 13,  8, 11,  5,  6, 15,  0,  3,  4,  7,  2, 12,  1, 10, 14,  9 },
                    { 10,  6,  9,  0, 12, 11,  7, 13, 15,  1,  3, 14,  5,  2,  8,  4 },
                    {  3, 15,  0,  6, 10,  1, 13,  8,  9,  4,  5, 11, 12,  7,  2, 14 }},
            // S5
            {{  2, 12,  4,  1,  7, 10, 11,  6,  8,  5,  3, 15, 13,  0, 14,  9 },
                    { 14, 11,  2, 12,  4,  7, 13,  1,  5,  0, 15, 10,  3,  9,  8,  6 },
                    {  4,  2,  1, 11, 10, 13,  7,  8, 15,  9, 12,  5,  6,  3,  0, 14 },
                    { 11,  8, 12,  7,  1, 14,  2, 13,  6, 15,  0,  9, 10,  4,  5,  3 }},
            // S6
            {{ 12,  1, 10, 15,  9,  2,  6,  8,  0, 13,  3,  4, 14,  7,  5, 11 },
                    { 10, 15,  4,  2,  7, 12,  9,  5,  6,  1, 13, 14,  0, 11,  3,  8 },
                    {  9, 14, 15,  5,  2,  8, 12,  3,  7,  0,  4, 10,  1, 13, 11,  6 },
                    {  4,  3,  2, 12,  9,  5, 15, 10, 11, 14,  1,  7,  6,  0,  8, 13 }},
            // S7
            {{  4, 11,  2, 14, 15,  0,  8, 13,  3, 12,  9,  7,  5, 10,  6,  1 },
                    { 13,  0, 11,  7,  4,  9,  1, 10, 14,  3,  5, 12,  2, 15,  8,  6 },
                    {  1,  4, 11, 13, 12,  3,  7, 14, 10, 15,  6,  8,  0,  5,  9,  2 },
                    {  6, 11, 13,  8,  1,  4, 10,  7,  9,  5,  0, 15, 14,  2,  3, 12 }},
            // S8
            {{ 13,  2,  8,  4,  6, 15, 11,  1, 10,  9,  3, 14,  5,  0, 12,  7 },
                    {  1, 15, 13,  8, 10,  3,  7,  4, 12,  5,  6, 11,  0, 14,  9,  2 },
                    {  7, 11,  4,  1,  9, 12, 14,  2,  0,  6, 10, 13, 15,  3,  5,  8 },
                    {  2,  1, 14,  7,  4, 10,  8, 13, 15, 12,  9,  0,  3,  5,  6, 11 }}
    };

    public int[] encrypt(int[] plaintext, int[] key) {
        int[][] roundKeys = generateRoundKeys(key);
        return cipher(plaintext, roundKeys);
    }

    public int[] decrypt(int[] ciphertext, int[] key) {
        int[][] roundKeys = generateRoundKeys(key);
        int[][] reversed  = new int[16][48];
        for (int i = 0; i < 16; i++) reversed[i] = roundKeys[15 - i];
        return cipher(ciphertext, reversed);
    }

    // Cipher (pseudocode)
    //
    // Cipher(plainBlock[64], RoundKeys[16,48], cipherBlock[64])
    // {
    //     permute(64, 64, plainBlock, inBlock, InitialPermutationTable)
    //     split(64, 32, inBlock, leftBlock, rightBlock)
    //     for (round = 1 to 16)
    //     {
    //         mixer(leftBlock, rightBlock, RoundKeys[round])
    //         if (round != 16) swapper(leftBlock, rightBlock)
    //     }
    //     combine(32, 64, leftBlock, rightBlock, outBlock)
    //     permute(64, 64, outBlock, cipherBlock, FinalPermutationTable)
    // }

    private int[] cipher(int[] plainBlock, int[][] roundKeys) {
        int[] inBlock    = new int[64];
        int[] leftBlock  = new int[32];
        int[] rightBlock = new int[32];

        permute(plainBlock, inBlock, IP);
        split(inBlock, leftBlock, rightBlock, 32);

        for (int round = 1; round <= 16; round++) {
            mixer(leftBlock, rightBlock, roundKeys[round - 1]);
            if (round != 16) swapper(leftBlock, rightBlock);
        }

        int[] outBlock    = new int[64];
        int[] cipherBlock = new int[64];
        combine(leftBlock, rightBlock, outBlock, 32);
        permute(outBlock, cipherBlock, FP);

        return cipherBlock;
    }

    // Mixer
    // mixer(leftBlock[32], rightBlock[32], RoundKey[48])
    // {
    //     copy(32, rightBlock, T1)
    //     function(T1, RoundKey, T2)
    //     exclusiveOr(32, leftBlock, T2, T3)
    //     copy(32, T3, leftBlock)
    // }

    private void mixer(int[] leftBlock, int[] rightBlock, int[] roundKey) {
        int[] t1 = new int[32];
        int[] t2 = new int[32];
        int[] t3 = new int[32];

        copy(rightBlock, t1);
        function(t1, roundKey, t2);
        exclusiveOr(leftBlock, t2, t3);
        copy(t3, leftBlock);
    }

    // Swapper
    // swapper(leftBlock[32], rightBlock[32])
    // {
    //     copy(32, leftBlock, T)
    //     copy(32, rightBlock, leftBlock)
    //     copy(32, T, rightBlock)
    // }

    private void swapper(int[] leftBlock, int[] rightBlock) {
        int[] t = new int[32];
        copy(leftBlock, t);
        copy(rightBlock, leftBlock);
        copy(t, rightBlock);
    }

    // Ffunction (Feistel function)
    //
    // function(inBlock[32], RoundKey[48], outBlock[32])
    // {
    //     permute(32, 48, inBlock, T1, ExpansionPermutationTable)
    //     exclusiveOr(48, T1, RoundKey, T2)
    //     substitute(T2, T3, SubstituteTables)
    //     permute(32, 32, T3, outBlock, StraightPermutationTable)
    // }

    private void function(int[] inBlock, int[] roundKey, int[] outBlock) {
        int[] t1 = new int[48];
        int[] t2 = new int[48];
        int[] t3 = new int[32];

        permute(inBlock, t1, E);
        exclusiveOr(t1, roundKey, t2);
        substitute(t2, t3);
        permute(t3, outBlock, P_TABLE);
    }

    // Substitute
    // substitute(inBlock[48], outBlock[32], SubstitutionTables[8][4][16])
    // {
    //     for (i = 1 to 8)
    //     {
    //         row = 2 × inBlock[(i-1)×6+1] + inBlock[(i-1)×6+6]
    //         col = 8×inBlock[(i-1)×6+2] + 4×inBlock[(i-1)×6+3]
    //               + 2×inBlock[(i-1)×6+4] + inBlock[(i-1)×6+5]
    //         value = SubstitutionTables[i][row][col]
    //         outBlock[(i-1)×4+1] < value/8;  value < value mod 8
    //         outBlock[(i-1)×4+2] < value/4;  value < value mod 4
    //         outBlock[(i-1)×4+3] < value/2;  value < value mod 2
    //         outBlock[(i-1)×4+4] < value
    //     }
    // }

    private void substitute(int[] inBlock, int[] outBlock) {
        for (int i = 0; i < 8; i++) {
            int base = i * 6;
            // Outer two bits of the 6-bit group select the row
            int row  = 2 * inBlock[base] + inBlock[base + 5];
            // Inner four bits select the column
            int col  = 8 * inBlock[base + 1] + 4 * inBlock[base + 2]
                    + 2 * inBlock[base + 3] +     inBlock[base + 4];

            int value   = SBOXES[i][row][col];
            int outBase = i * 4;

            outBlock[outBase]     = value / 8; value = value % 8;
            outBlock[outBase + 1] = value / 4; value = value % 4;
            outBlock[outBase + 2] = value / 2; value = value % 2;
            outBlock[outBase + 3] = value;
        }
    }

    // Key Schedule
    // K (64-bit) > PC-1 > 56-bit key > split into C0 (28) and D0 (28)
    // For each round i = 1..16:
    //   Ci = LS_i(C_{i-1}),  Di = LS_i(D_{i-1})
    //   K_i = PC-2(Ci || Di)

    public int[][] generateRoundKeys(int[] key) {
        int[] permutedKey = new int[56];
        permute(key, permutedKey, PC1);

        int[] c = new int[28];
        int[] d = new int[28];
        split(permutedKey, c, d, 28);

        int[][] roundKeys = new int[16][48];
        for (int round = 0; round < 16; round++) {
            c = leftCircularShift(c, SHIFTS[round]);
            d = leftCircularShift(d, SHIFTS[round]);

            int[] cd = new int[56];
            combine(c, d, cd, 28);
            permute(cd, roundKeys[round], PC2);
        }
        return roundKeys;
    }

    public int[] getPC1Result(int[] key) {
        int[] result = new int[56];
        permute(key, result, PC1);
        return result;
    }

    public int[] splitLeft(int[] block, int size) {
        int[] left = new int[size];
        DES.copy(block, 0, left, 0, size);
        return left;
    }

    public int[] splitRight(int[] block, int size) {
        int[] right = new int[size];
        DES.copy(block, size, right, 0, size);
        return right;
    }

    public int[] combineHalves(int[] left, int[] right) {
        int[] combined = new int[left.length + right.length];
        DES.copy(left, 0, combined, 0, left.length);
        DES.copy(right, 0, combined, left.length, right.length);
        return combined;
    }

    public int[] circularShift(int[] bits, int n) {
        return leftCircularShift(bits, n);
    }

    public int[] permuteData(int[] input, int[] table) {
        int[] output = new int[table.length];
        permute(input, output, table);
        return output;
    }

    public int[][] getKeySchedule(int[] key) {
        return generateRoundKeys(key);
    }

    public static int[] getSHIFTS() {
        return SHIFTS;
    }

    // permute: output[i] = input[table[i] - 1]  (table entries are 1-based)
    private void permute(int[] input, int[] output, int[] table) {
        for (int i = 0; i < table.length; i++) {
            output[i] = input[table[i] - 1];
        }
    }

    private void split(int[] block, int[] left, int[] right, int size) {
        DES.copy(block, 0, left, 0, size);
        DES.copy(block, size, right, 0, size);
    }

    private void combine(int[] left, int[] right, int[] block, int size) {
        DES.copy(left, 0, block, 0, size);
        DES.copy(right, 0, block, size, size);
    }

    private void exclusiveOr(int[] a, int[] b, int[] result) {
        for (int i = 0; i < result.length; i++) {
            result[i] = a[i] ^ b[i];
        }
    }

    public static void copy(int[] src, int[] dst) {
        for (int i = 0; i < src.length; i++)
            dst[i] = src[i];
    }

    public static void copy(int[] src, int srcPos, int[] dst, int dstPos, int length) {
        for (int i = 0; i < length; i++)
            dst[dstPos + i] = src[srcPos + i];
    }

    private int[] leftCircularShift(int[] bits, int n) {
        int[] result = new int[bits.length];
        for (int i = 0; i < bits.length; i++) {
            result[i] = bits[(i + n) % bits.length];
        }
        return result;
    }

}
