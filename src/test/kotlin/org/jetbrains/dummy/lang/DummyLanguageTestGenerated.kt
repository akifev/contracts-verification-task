package org.jetbrains.dummy.lang

import org.junit.Test

class DummyLanguageTestGenerated : AbstractDummyLanguageTest() {
    @Test
    fun testBad() {
        doTest("testData/bad.dummy")
    }
    
    @Test
    fun testBadFunctionSignature() {
        doTest("testData/badFunctionSignature.dummy")
    }
    
    @Test
    fun testBadInnerBlock() {
        doTest("testData/badInnerBlock.dummy")
    }
    
    @Test
    fun testGood() {
        doTest("testData/good.dummy")
    }
    
    @Test
    fun testGoodFunctionSignature() {
        doTest("testData/goodFunctionSignature.dummy")
    }
    
    @Test
    fun testGoodInnerBlock() {
        doTest("testData/goodInnerBlock.dummy")
    }
}
