#include "testwindow.h"

#include <sdl/imguiauxiliary.h>

#include <spdlog/spdlog.h>

TestWindow::TestWindow() {
	sdl::Window::setSize(512, 512);
	sdl::Window::setTitle("Test");
	sdl::ImGuiWindow::setShowDemoWindow(true);
	sdl::ImGuiWindow::setShowColorWindow(true);
}

void TestWindow::imGuiUpdate(const sdl::DeltaTime& deltaTime) {
	ImGui::MainWindow("Main", [&]() {
		ImGui::Button("Hello", {100, 100});
		ImGui::Button("World", {50, 50});
	});
}

void TestWindow::imGuiPreUpdate(const sdl::DeltaTime& deltaTime) {
}

void TestWindow::imGuiPostUpdate(const sdl::DeltaTime& deltaTime) {
}

void TestWindow::imGuiEventUpdate(const SDL_Event& windowEvent) {
	switch (windowEvent.type) {
		case SDL_WINDOWEVENT:
			switch (windowEvent.window.event) {
				case SDL_WINDOWEVENT_LEAVE:
					break;
				case SDL_WINDOWEVENT_CLOSE:
					sdl::Window::quit();
					break;
			}
			break;
		case SDL_QUIT:
			sdl::Window::quit();
			break;
		case SDL_KEYDOWN:
			switch (windowEvent.key.keysym.sym) {
				case SDLK_ESCAPE:
					sdl::Window::quit();
					break;
			}
			break;
	}
}
