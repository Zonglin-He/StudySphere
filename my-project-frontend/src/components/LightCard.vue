<template>
    <div class="light-card">
        <slot/>
    </div>
</template>

<style scoped>
.light-card {
    background: rgba(255, 255, 255, 0.82);
    border-radius: 20px;
    padding: 18px;
    box-shadow: 0 18px 36px rgba(15, 23, 42, 0.12);
    border: 1px solid rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(20px);
    transition: transform .25s ease, box-shadow .25s ease;
}

.light-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 22px 44px rgba(15, 23, 42, 0.16);
}

:global(.dark) .light-card {
    background: rgba(24, 32, 52, 0.78);
    border: 1px solid rgba(148, 163, 255, 0.2);
    box-shadow: 0 22px 44px rgba(2, 6, 23, 0.65);
}
</style>
