#include "testwindow.h"

#include <sdl/initsdl.h>

int main(int argc, char** argv) {
	sdl::InitSdl sdl{SDL_INIT_VIDEO};

    TestWindow{}.startLoop();

	return 0;
}
