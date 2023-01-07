#include <gtest/gtest.h>

class Test : public ::testing::Test {
protected:

	Test() {
	}

	~Test() override {
	}

	void SetUp() override {
	}

	void TearDown() override {
	}
};

TEST_F(Test, test_function) {
	// Given.
	bool isTrue = true;

	// Then.
	EXPECT_TRUE(isTrue);
}
