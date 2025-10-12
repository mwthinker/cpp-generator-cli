#ifndef TESTIMGUIWINDOW_H
#define TESTIMGUIWINDOW_H

#include <sdl/window.h>

class TestWindow : public sdl::Window {
public:
	TestWindow();

private:
	void processEvent(const SDL_Event& windowEvent) override;

	void renderImGui(const sdl::DeltaTime& deltaTime) override;
};

#endif
