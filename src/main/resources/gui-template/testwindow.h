#ifndef TESTIMGUIWINDOW_H
#define TESTIMGUIWINDOW_H

#include <sdl/imguiwindow.h>

class TestWindow : public sdl::ImGuiWindow {
public:
	TestWindow();

private:
	void imGuiPreUpdate(const sdl::DeltaTime& deltaTime) override;

	void imGuiPostUpdate(const sdl::DeltaTime& deltaTime) override;

	void imGuiEventUpdate(const SDL_Event& windowEvent) override;

	void imGuiUpdate(const sdl::DeltaTime& deltaTime) override;
};

#endif
