#include "testwindow.h"

#include <imgui.h>
#include <spdlog/spdlog.h>

TestWindow::TestWindow() {
	setSize(512, 512);
	setTitle("Test");
	setShowDemoWindow(true);
	setShowColorWindow(true);
}

void TestWindow::renderImGui(const sdl::DeltaTime& deltaTime) {
	ImGui::MainWindow("Main", [&]() {
		ImGui::Button("Hello", {100, 100});
		ImGui::Button("World", {50, 50});
	});
}

void TestWindow::processEvent(const SDL_Event& windowEvent) {
	switch (windowEvent.type) {
		case SDL_EVENT_WINDOW_CLOSE_REQUESTED:
			sdl::Window::quit();
			break;
		case SDL_EVENT_QUIT:
			sdl::Window::quit();
			break;
		case SDL_EVENT_KEY_DOWN:
			switch (windowEvent.key.key) {
				case SDLK_ESCAPE:
					sdl::Window::quit();
					break;
			}
			break;
	}
}
